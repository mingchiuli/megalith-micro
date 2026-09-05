# Search and Cache Operations

## Normal Operation

MariaDB remains the write store. All administration searches, filters, counts, and exports use
Elasticsearch to select IDs, totals, and ordering; current content and sensitive ranges are fetched
from MariaDB and checked against the current user scope and filters. Missing or changed records can
temporarily produce short pages while the index catches up. Elasticsearch failures are returned to
the caller.

Blog changes arrive through the transactional outbox. Content scripts compare `_source.revision`
with the blog's `event_revision`; they ignore duplicate or older revisions and retain deletion
tombstones. Statistics updates never modify this revision. MariaDB cumulative read counts are
synchronized by `micro-blog` in ID order, using absolute values and monotonic updates rather than
increments. Missing documents and tombstones are skipped and a later cycle retries failed batches.

| Setting | Default | Purpose |
| --- | --- | --- |
| `megalith.blog.statistics-sync.enabled` | `true` | Enable cumulative count synchronization |
| `megalith.blog.statistics-sync.interval` | `60s` | Delay between synchronization cycles |
| `megalith.blog.statistics-sync.batch-size` | `500` | Rows per request, maximum 1000 |
| `megalith.blog.maintenance.read-only` | `false` | Reject content writes and pause user-deletion consumption and count synchronization |
| `megalith.search.maintenance.enabled` | `false` | Pause search event consumption and allow rebuild requests |
| `megalith.search.index.alias` | `blog_search` | Alias used for search and index writes |
| `megalith.search.index.legacy-name` | `blog_index_v4` | Existing physical index used when initializing the alias |
| `megalith.search.index.rebuild-batch-size` | `500` | Snapshot rows per batch, maximum 1000 |

`micro-search` uses the existing Redis service for maintenance locks. Configure Redis and the blog
RPC address directly in the environment's application YAML, following the other services. The local
`application-dev.yml` contains:

```yaml
spring:
  data:
    redis:
      port: 6379
      password: 123456
      host: 127.0.0.1
megalith:
  blog:
    blog-url: http://127.0.0.1:8082/inner
```

Set the deployed addresses and credentials in the deployment's environment configuration. Use the
same `megalith.task-lock.environment` in blog and search services.

The internal contracts are:

- `POST /inner/blog/views/batch` on `micro-search`: a list of `{blogId, readCount}` cumulative values.
- `GET /inner/blog/index/status` on `micro-blog`: read-only mode, ready/paused BLOG outbox rows, and total rows.
- `GET /inner/blog/index/snapshots?afterId=0&limit=500` on `micro-blog`: complete snapshots including revision.
- `POST /inner/search/index/rebuild` on `micro-search`: synchronous maintenance rebuild.

These are internal-network operations and must not be added to public gateway routes. Production
continues to run native executables; no script interpreter, JRE, or source tree is required.

## Coordinated Upgrade

This upgrade removes the search service's per-view increment endpoint and the blog API's
pagination-invalidation count endpoint. Blog events no longer carry `totalCount`, `newerOrSameCount`,
or `previousTotalCount`. The existing JSON message converter ignores those fields in queued old events.

1. Pause public content mutations, deploy `micro-blog` with read-only maintenance enabled, and wait
   for all old blog instances and their in-flight writes to finish. User-deletion messages remain
   queued while the blog consumer is paused. Keep the statistics scheduler disabled until search is upgraded.
2. Stop old search consumers and replace all `micro-search` instances with the new binary, initially
   in normal mode so it can drain historical events. Startup initializes `blog_search` to the existing
   `blog_index_v4`, or creates an empty physical index on a fresh installation. Existing physical
   indexes are not deleted.
3. Replace every `micro-exhibit` instance together. Page caches now use `blog-page:v3` with key
   tracking; old and new page-cache consumers must not overlap. Detail and sensitive caches retain
   their existing contracts. Old page values can expire naturally.
4. Confirm the BLOG outbox has zero ready and paused rows. Resolve any historical search DLQ entries
   and let search processing drain the main and retry queues. Do not purge unrelated queues.
5. Follow the rebuild procedure below, then restore normal operation. After content writes resume,
   an old consumer binary that requires the removed event fields is not a valid rollback target.

## Rebuild Procedure

1. Set `megalith.blog.maintenance.read-only=true` on every blog replica and finish their graceful
   replacement. Keep blog HTTP reads and its outbox publisher available. Verify the status endpoint
   reports `readOnly=true`, `readyEvents=0`, and `pausedEvents=0`.
2. While search consumers still run, resolve the search DLQ and wait for all search main/retry
   messages to complete. The relevant queues are `blog.change.queue.es`, its `.retry.1`, `.retry.2`,
   `.retry.3` queues, and `.dlq`. Check both ready and unacknowledged message counts in RabbitMQ.
3. Set `megalith.search.maintenance.enabled=true` on every search replica and finish their graceful
   replacement. Verify there are no old handlers still running, no unacknowledged messages, and no
   consumers on the search queue. Existing searches can continue against the current alias.
4. Invoke the rebuild on one search replica over the internal network:

   ```bash
   curl --fail-with-body --max-time 600 -X POST http://127.0.0.1:8085/inner/search/index/rebuild
   ```

5. The request verifies maintenance mode, source readiness, and empty/stopped search queues, then
   acquires the shared rebuild and statistics-sync locks. It writes snapshots into a new
   `blog_search-v5-<uuid>` index, checks batch results, source counts, and refreshed index counts,
   and atomically switches the alias. The response includes `previousIndex`, `index`, and `documents`.
6. Validate searches and a known recently changed/deleted article while writes are still paused.
   Set search maintenance to false first, then blog read-only mode to false. Queued user deletions
   and regular content events resume, and count synchronization catches up on its next cycle.

The maintenance switches are deployment settings, not cluster-wide toggles. All replicas must use
them consistently. A request rejected before index creation leaves the alias untouched. Bulk or
validation failures retain the old alias and leave the new candidate index available for inspection.
If the HTTP connection or Elasticsearch connection fails during alias activation, inspect the alias
before retrying because the switch may already have completed.

Old physical indexes are retained. An alias rollback is appropriate only during the same paused
validation window; after writes resume, rebuild from the current source before switching back.
Remove only explicitly identified unused indexes after inspection. Rebuild does not automatically
replay or discard dead-letter messages.

## Tracked Page Caches

All pages remain cacheable. `@Cache(trackKeys=true)` registers exact keys before loading or promoting
an L2 value, under the same distributed key lock used by eviction. Content events snapshot the
`blog-page:v3` registry and invalidate its keys in batches of 256, including pages beyond the current
last page. Event revision is recorded only after all batches and other event effects succeed.

Registry membership outlives cached values and is not removed by successful eviction. This permits
safe retries and covers other replicas' L1 entries. Registration failures return source data without
creating untracked L1/L2 entries. L1 hits retain their existing fast path.

After Redis data loss, clear all tracked-cache replicas' L1 by restarting them. If L2 data survived
but its directory did not, stop the replicas and invalidate the exact keys obtained from a targeted
scan of `blog-page:v3:*` before restarting; preserve unrelated Redis data. Old-version directories
can be removed only after every replica and value using that version has retired.

## Verification and Metrics

```bash
./gradlew build
./gradlew :micro-blog:integrationTest :micro-search:integrationTest
./gradlew :cache:integrationTest --tests '*TrackedCacheIntegrationTest' \
  --tests '*RedisCacheEvictionIntegrationTest' --tests '*RabbitCacheEvictionIntegrationTest'
```

Integration tests use isolated MariaDB, Elasticsearch, Redis, and RabbitMQ containers. The search
fixture supplies standard analyzers under the production mapping's IK names to test update and
alias behavior without changing the production IK configuration.

Monitor `megalith.blog.statistics.sync.*` and `megalith.search.rebuild.*`: `failures`, `documents`,
`duration`, and `last.success` (Unix epoch seconds). Cache registration failures use the existing
`megalith.cache.failures` meter; normal eviction metrics and correlated HTTP traces remain active.
