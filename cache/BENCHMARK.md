# Cache Local Middleware Benchmark

Measured on 2026-08-25 against the developer machine's running Redis and RabbitMQ. These results
are a local regression baseline, not a production capacity guarantee.

## Result

The redesigned cache passed the repository build, cache unit and container integration suites, and
three fresh-JVM runs against the local middleware. Distributed single-flight collapsed 128
concurrent calls across two replicas to one source load. A concurrent exact eviction waited for an
in-flight load, then removed both replicas' L1 entries and the L2 value. TTL and both eviction
transports behaved as specified.

## Environment

- Apple M4 Pro, 12 cores, 64 GB RAM, macOS 26.6.2 arm64
- GraalVM 25.2.4, Java 25.0.4 HotSpot
- Docker 29.7.2 with 12 CPUs and 64 GB available to the VM
- Redis 8.10.0 arm64, loopback port 6379, 64 MiB container limit
- RabbitMQ 4.3.4, loopback port 5672, 512 MiB container limit

Redis reported `maxmemory=0` and `maxmemory-policy=noeviction`. That is adequate for this short
test, but the 64 MiB container can be killed before Redis performs controlled eviction during a
memory saturation test. Configure an explicit Redis memory limit and eviction policy, and give the
container suitable headroom, before running a soak or production-capacity benchmark.

## Method

The local benchmark uses the production `CacheAspect`, key factory, Redisson locks, exact
evictors, Redis reliable topics, Rabbit publisher confirms, and two independent L1 caches. Each
reported row is the median of three fresh test-worker runs; the parenthesized values are the minimum
and maximum across those runs.

Operations are executed serially when collecting per-call latency distributions. Consequently, the
derived throughput in the table is single-caller throughput, not middleware saturation throughput.
The separate `redis-benchmark` result uses 50 connections and four threads as a server baseline.

| Path | Samples per run | Throughput ops/s | p50 | p95 | p99 |
| --- | ---: | ---: | ---: | ---: | ---: |
| Canonical typed JSON + SHA-256 key | 100,000 | 1,181,038 (1,108,214-1,257,407) | 0.750 us (0.625-0.750) | 1.417 us (1.250-1.500) | 1.834 us (1.625-1.875) |
| Direct Redisson GET | 5,000 | 6,884 (6,835-6,927) | 140.500 us (138.750-142.375) | 186.125 us (185.833-186.708) | 214.375 us (214.042-217.334) |
| Full aspect L1 hit | 100,000 | 533,305 (517,631-534,735) | 1.542 us (1.375-1.666) | 3.792 us (3.791-3.834) | 5.417 us (5.375-5.458) |
| Full aspect L2 hit | 2,000 | 6,056 (5,852-6,083) | 155.291 us (153.875-160.666) | 220.959 us (216.375-226.583) | 269.584 us (265.959-287.291) |
| Redis reliable-topic exact eviction | 50 | 737 (719-747) | 1.344 ms (1.306-1.379) | 1.602 ms (1.541-1.602) | 2.023 ms (1.978-2.239) |
| Rabbit confirmed-fanout exact eviction | 50 | 575 (555-579) | 1.517 ms (1.483-1.581) | 1.910 ms (1.865-1.925) | 12.747 ms (12.020-12.862) |

The full L2 hit added about 14.8 us at p50 over a direct Redisson GET on this machine. The full L1
path, including annotation advice and key generation, stayed below 5.5 us at p99 in all three runs.

With only 50 eviction samples per run, p99 is the maximum observation rather than a statistically
strong percentile. Rabbit had one roughly 12 ms tail observation in every run while p95 remained
below 2 ms; a longer production-like run is required before assigning an SLO to that tail.

The native Redis baseline was approximately 199,601 SET/s at p50 0.167 ms and 199,601 GET/s at p50
0.159 ms with 100,000 operations, 50 connections, and four threads. This shows why the serial
Redisson throughput above must not be treated as the Redis server's capacity.

## Correctness Checks

| Check | Observed result |
| --- | --- |
| Distributed single-flight | 128 callers, two replicas, exactly one source load; 85.297 ms median total with a 75 ms source |
| Redis TTL | 400 ms configured, 399-400 ms initial TTL, 401.972 ms median observed expiry, then a second source load |
| Load versus eviction race | Eviction waited for the load lock; two L1 entries and L2 were absent; next read reloaded; 57.030 ms median |
| Redis reliable-topic eviction | Exact L2 delete and remote L1 invalidation succeeded in all 150 measured operations |
| Rabbit confirmed fanout | Exact L2 delete and two remote L1 invalidations succeeded in all 150 measured operations |
| Cleanup | Zero `cache-benchmark-*` keys/locks and zero benchmark Rabbit queues/exchanges remained |

Verification completed with 26 cache unit tests, two Testcontainers integration tests, three local
middleware benchmark runs, and the full 148-task repository build including service tests, ArchUnit,
and Spring AOT test compilation. All completed with zero failures.

## Reproduce

The benchmark is opt-in so ordinary CI remains independent of developer middleware. Supply local
credentials through environment variables; do not commit them.

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-25.2.4+7.1/Contents/Home
CACHE_LOCAL_MIDDLEWARE=true \
CACHE_LOCAL_REDIS_PASSWORD='<redis-password>' \
CACHE_LOCAL_RABBIT_USERNAME='<rabbit-username>' \
CACHE_LOCAL_RABBIT_PASSWORD='<rabbit-password>' \
./gradlew :cache:integrationTest --tests '*LocalMiddlewareCacheBenchmarkTest' --rerun-tasks
```

The test uses unique `cache-benchmark-*` resources and removes them in `finally` blocks. It does not
flush Redis or delete application Rabbit exchanges and queues.
