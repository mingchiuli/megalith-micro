# Megalith Cache Starter

`megalith-cache-spring-boot-starter` provides Caffeine L1 caching, Redis L2 caching, per-key
single-flight loading, and replica-wide exact eviction. It is a JPMS module and supports Spring AOT
and GraalVM Native Image.

## Contract

Applications declare a stable cache identity independently of Java class and method names:

```java
@Cache(namespace = "auth-user-access", version = 1, ttl = 30, timeUnit = TimeUnit.MINUTES)
public UserAccessRpcVo getUserAccess(Long userId) {
  return userDirectory.findUserAccess(userId);
}
```

The key format is:

```text
<namespace>:v<version>:<sha256(canonical typed argument JSON)>
```

Namespaces use lowercase letters, digits, dots, and hyphens. A namespace and version pair may be
owned by only one cached method in an application; startup fails on reuse. Increase `version` for
every incompatible argument, serialization, or return-value contract change. Method renames do not
change keys.

Use the same `CacheDescriptor` constant for annotations and explicit eviction:

```java
public static final CacheDescriptor USER_ACCESS = new CacheDescriptor("auth-user-access", 1);

cacheEvictor.evict(Set.of(cacheKeyFactory.generate(USER_ACCESS, userId)));
```

Cached methods must pass through their Spring proxy. `null` is returned but never stored. L1 entries
receive 90-100% of the annotation TTL at insertion. Promoting an L2 value starts a new local TTL,
so local entries can remain after the corresponding Redis value expires.

## Tracked Keys

Enable `trackKeys = true` for contracts whose callers cannot enumerate all cached arguments:

```java
@Cache(namespace = "blog-page", version = 3, trackKeys = true)
public PageAdapter<BlogDescriptionDto> findPage(Integer currentPage) {
    return loadPage(currentPage);
}

cacheEvictor.evict(cacheKeyRegistry.registeredKeys(new CacheDescriptor("blog-page", 3)));
```

`CacheKeyRegistry` is exposed in the public `handler` package. The registry stores exact keys in
`megalith:cache:keys:<namespace>:v<version>`. Tracked L1 misses acquire the existing distributed key
lock and register before reading L2 or the source, keeping registration and promotion ordered with
eviction. L1 hits require no extra Redis call. Registry, remote-read, or remote-lock failures fall
back to the source without caching an untracked local value.

Directory membership survives normal TTL expiry and explicit eviction because other replicas may
still retain local values or refill a key. A newly registered key whose first load fails or returns
null is removed under the same lock. Successful registrations are retained for that contract version;
applications should evict large snapshots in bounded batches and retry the complete snapshot after
a partial failure. Directories for retired versions can be removed after all old replicas and values
are gone. Bump the contract version when first enabling tracking on an existing cache.

After complete Redis data loss, restart all application replicas using tracked caches to clear L1.
If only a directory was lost, stop those replicas and invalidate the exact remaining L2 keys for
that namespace/version before restarting. New loads recreate directory membership.

## Required Beans

The application must provide a configured `RedissonClient` bean. The starter intentionally does not
own Redis addresses, credentials, topology, or client lifecycle. A Spring Boot `JsonMapper` is also
required and is provided by `spring-boot-starter-jackson`.

RabbitMQ eviction additionally requires `spring-boot-starter-amqp`, a `ConnectionFactory` with
publisher confirms and returns enabled, and the Rabbit properties below. `AUTO` selects Rabbit only
when those Rabbit cache properties and a connection factory are present; otherwise it uses the Redis
reliable topic transport.

## Configuration

```yaml
megalith:
  cache:
    local:
      initial-capacity: 512
      maximum-entries: 12400
      ttl-jitter: 0.1
    single-flight:
      wait-timeout: 5s
      lock-entry-ttl: 30m
    eviction:
      transport: AUTO # AUTO, RABBIT, or REDIS
      rabbit:
        queue-prefix: cache.auth.evict.queue.
        exchange: cache.auth.evict.fanout.exchange
        confirm-timeout: 5s
      redis:
        topic: megalith.cache.evict
```

Rabbit queues are exclusive, auto-delete queues created per replica. The exchange is durable.
Rabbit reconnection clears the entire local cache because messages may have been missed while the
replica was disconnected.

## Failure Semantics

| Operation | Behavior |
| --- | --- |
| Redis L2 read or lock unavailable | Run the source method and populate only L1 |
| Tracked cache registry, L2 read, or lock unavailable | Run the source method without caching |
| Local or remote load lock timeout | Run the source method without caching |
| Cached value cannot be decoded | Delete it best-effort and reload from the source |
| Source returns `null` | Return `null`; do not write L1 or L2 |
| Cache serialization or L2 write fails | Return the source result and record a failure metric |
| Eviction lock, Redis delete, or broadcast fails | Throw; eviction is fail-closed |

Multi-key eviction sorts and acquires all distributed locks before deleting any key. It keeps those
locks through exact Redis deletion, local invalidation, and confirmed broadcast, then releases them
in reverse order.

Micrometer meters are emitted when a `MeterRegistry` is available:

- `megalith.cache.requests` with `result=l1_hit|l2_hit|source`
- `megalith.cache.failures` with an `operation` tag
- `megalith.cache.lock.timeouts` with a `scope` tag
- `megalith.cache.evictions` with `transport` and `result` tags

## Breaking Upgrade Runbook

This version has no compatibility layer for the former method-derived keys, `CacheEvictHandler`,
`CommonCacheKeyGenerator`, cache-owned Redisson configuration, or the cache `@Checker` aspect.

1. Stop every `micro-auth` and `micro-exhibit` replica. Do not run old and new binaries together.
2. Remove only the former cache key families from Redis. Preserve unrelated Redis state such as
   tokens, bloom filters, visit counters, and collaboration data.
3. Deploy all replicas with the new annotations, descriptor constants, and eviction configuration.
4. Confirm Rabbit publisher confirms/returns or the Redis reliable topic, then start traffic.

The former key families are:

```text
user_access:AuthWrapper:getUserAccess:*
role_authorization:AuthWrapper:getAllRoleAuthorizations
role_authority:AuthWrapper:getCurrentUserNav:*
all_service:AuthWrapper:getAllSystemAuthorities
hot_blog:BlogWrapper:findById:*
hot_blog:BlogSensitiveWrapper:findSensitiveByBlogId:*
hot_blogs:BlogWrapper:findPage:*
```

For a standalone Redis instance, delete them with targeted scans rather than `FLUSHDB`:

```bash
for pattern in \
  'user_access:AuthWrapper:getUserAccess:*' \
  'role_authorization:AuthWrapper:getAllRoleAuthorizations' \
  'role_authority:AuthWrapper:getCurrentUserNav:*' \
  'all_service:AuthWrapper:getAllSystemAuthorities' \
  'hot_blog:BlogWrapper:findById:*' \
  'hot_blog:BlogSensitiveWrapper:findSensitiveByBlogId:*' \
  'hot_blogs:BlogWrapper:findPage:*'
do
  redis-cli --scan --pattern "$pattern" | while IFS= read -r key
  do
    redis-cli UNLINK "$key"
  done
done
```

Run the equivalent targeted scan on every primary when Redis Cluster is used.

## Verification

```bash
./gradlew :cache:check
./gradlew :cache:integrationTest
```

The integration suite uses Testcontainers to verify Redis reliable-topic invalidation and confirmed
RabbitMQ fanout across two local-cache replicas.

For the opt-in test against already-running local middleware, including latency distributions,
single-flight, TTL, load-versus-eviction ordering, cleanup, and interpretation, see
[BENCHMARK.md](BENCHMARK.md).
