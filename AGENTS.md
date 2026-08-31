# AGENTS.md - Megalith Micro

Operating notes for AI coding agents working in this repository. Read [README.md](README.md) for
the complete architecture, application responsibilities, deployment model, and frontend SSR flow;
this file records the implementation constraints that are easy to miss.

## Toolchain

- Java 25 (GraalVM HotSpot), Spring Boot 4.1.1, Hibernate ORM 7.4.6.Final, Redisson 4.7.0, and Caffeine.
- Gradle 9.7 Kotlin DSL; the root `build.gradle.kts` configures all Java subprojects.
- Rust 2024 for `micro-gateway-rs` and `micro-sync-rs`.
- Bun 1.4.0, Vue 3, and Vite for the standalone `micro-frontend` service.

`JAVA_HOME` must point to a GraalVM HotSpot JDK, not the Espresso JVM. On macOS, for example:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-25.3.4.1+1.1/Contents/Home
```

## Checks and Commands

```bash
./gradlew build
./gradlew :micro-auth:test
./gradlew :micro-auth:nativeCompile
cargo fmt --all -- --check
cargo clippy --workspace --all-targets -- -D warnings
cargo test --workspace
# Requires Redis 8 on 127.0.0.1:6379.
MICRO_SYNC_TEST_REDIS_URL=redis://127.0.0.1:6379/ \
  cargo test -p micro-sync-rs redis_store_round_trip_when_configured -- --ignored
bun install --frozen-lockfile
bun run frontend:check
bun run frontend:build
```

Java tests include ArchUnit and Spring AOT processing; keep reflection and runtime hints in sync.
Java production artifacts are GraalVM Native Images, Rust services are release binaries, and the
frontend is a Bun standalone executable. Do not add deployment assumptions that require a JVM, JRE,
Rust toolchain, Bun runtime, or source tree in a production image.

## Repository Shape

| Area | Modules | Responsibility |
| --- | --- | --- |
| Applications | `micro-auth`, `micro-user`, `micro-blog`, `micro-exhibit`, `micro-search` | Java native services |
| Rust applications | `micro-gateway-rs`, `micro-sync-rs` | Gateway proxy and Redis-backed collaboration |
| Frontend | `micro-frontend` | Vue SSR, hydration, and embedded static assets |
| Contracts | `api-auth`, `api-user`, `api-blog`, `api-search` | Typed HTTP interfaces and RPC models |
| Shared Java | `common-*` | Contract, RPC, web, auth, observability, messaging, scheduling, outbox, export |
| Cache | `cache` | Caffeine L1, Redis L2, and distributed eviction |

The frontend is an independent Bun workspace outside Gradle and Cargo. Dependency versions belong
in the root `package.json` catalog and `bun.lock`; workspace packages use `catalog:` references.

## Architecture Invariants

1. **Single-pass authorization.** `micro-gateway-rs` calls `POST /inner/auth/route` once. The auth
   service returns the target host/port and a Base64URL principal in `X-Megalith-Principal`; business
   services trust that header and never re-derive identity from cookies.
2. **SSR and browser traffic are separate.** SSR prefetch uses `SSR_API_BASE_URL` through the gateway.
   Browser API and WebSocket traffic goes through nginx and the gateway. Tokens stay in HttpOnly
   cookies; browser code must not read or persist access or refresh tokens. SSR requests must create
   isolated Router, Pinia, i18n, head-management, and HTTP state.
3. **Stateless collaboration.** `micro-sync-rs` replicas coordinate through shared Redis Streams and
   state for documents, awareness, snapshots, presence, leases, and compaction. Do not add sticky
   session or room ownership assumptions.
4. **`@Cache` owns cache keys.** Keys are
   `<namespace>:v<version>:<sha256(canonicalTypedArgs)>` from `CacheKeyFactory`. Cache methods must go
   through the Spring proxy. Reads are Caffeine L1 then Redis L2; misses execute the method and write
   both levels with the annotation TTL. Increase the explicit version for incompatible contracts.
5. **Eviction is exact and broadcast.** `AuthCacheKeys`/`CacheEvictor` use shared
   `CacheDescriptor` constants, acquire the same distributed key locks as reads, delete exact Redis
   keys, invalidate local L1, and broadcast through confirmed RabbitMQ fanout or a Redis reliable
   topic. There are no reflective method lookups in eviction.
6. **Ports and adapters.** Core Java code uses `domain`, `application.model`,
   `application.port.in`, `application.port.out`, `application.service`, `adapter.in.*`,
   `adapter.out.*`, and `config`. Input adapters call input ports; application services depend on
   output ports, never concrete HTTP, persistence, Redis, Elasticsearch, or storage adapters.
   `*HttpServiceWrapper` classes unwrap `RemoteResult.requireSuccess(...)` behind directory/gateway
   ports. Spring Data repositories belong under `adapter.out.persistence.repository`.
7. **Transaction boundary.** Services prepare inputs across reads without a transaction.
   Transactional persistence adapters (including existing `*Wrapper` classes) do only writes and
   the matching outbox insert in one short transaction and never query back. Do not add
   `@Transactional` to services or repository reads to transactional writers.
8. **Transactional outbox.** User and blog changes commit to `m_outbox_event` before confirmed
   RabbitMQ publication. Cache eviction and Elasticsearch indexing consume those events. User
   deletion uses `UserDeletedMessage` on its dedicated fanout exchange; `micro-blog` consumes it to
   delete blogs owned by the deleted users. Never publish domain events outside the outbox.
9. **JPMS.** `cache` exports only its public `annotation`, `handler`, and `key` packages. A new
   public package needs an `exports` entry; downstream JPMS modules require `wiki.chiu.micro.cache`.
10. **Native and AOT reachability.** Types used through reflection, serialization, HTTP interfaces, or
    native-image initialization need the matching Spring AOT/runtime hints.
11. **Observability.** Java, Rust, Bun, gateway, and sync services export correlated OpenTelemetry
    traces, metrics, and logs; preserve existing trace-context propagation when adding boundaries.
12. **External Lua sources.** Redis scripts live in standalone `.lua` files. Java reads them as
    classpath resources and registers the required Native Image resource hints; Rust loads them with
    `include_str!`. Never embed Lua bodies in Java or Rust string literals, and do not require a
    source tree beside a production executable.
13. **CI and rewritten history.** A force-push event may provide a `github.event.before` SHA that is
   no longer reachable. `fetch-depth: 0` does not fetch deleted objects. Workflows diffing event SHAs
   must verify `git cat-file -e "$BEFORE^{commit}"` and use a conservative full-build path when it
   is missing. Do not rewrite shared history without explicit approval, a complete `git bundle` backup,
   and `--force-with-lease`; old commit URLs, PR refs, caches, and local stashes may retain old objects.
14. **Rust boundaries match service responsibilities.** `micro-sync-rs` keeps protocol and state in
    `domain`, orchestration and store traits in `application`, Axum delivery in `adapter.inbound`, and
    Redis connections, errors, keys, Stream IDs, workers, and Lua calls in `adapter.outbound.redis`.
    Inbound adapters call `RoomManager` rather than the concrete store. `micro-gateway-rs` remains a
    transport-oriented edge service: authentication belongs to `client`, no-I/O forwarding rules
    belong to `proxy`, and handlers/middleware coordinate them without an artificial domain layer.

## Code Style

- Java uses 4-space indentation, sorted imports, no unused imports, and the surrounding method and
  comment order. Preserve Chinese Javadoc conventions in `micro-user`.
- A Java source file's physical directory must match its declared package in both `src/main/java`
  and `src/test/java`; Gradle may compile mismatches, but JDTLS reports them as errors.
- RPC models are records in `api-*`; preserve established hand-written builders such as
  `AuthorityRpcVo.builder()`.
- Nullability uses `org.jspecify` `@NonNull`. Jackson is Jackson 3's `tools.jackson.databind.JsonMapper`,
  not `com.fasterxml.jackson`.
- Server WebMVC is functional: use `*Handler` with `RouterFunctions`, keep internal routes under
  `/inner/...`, and declare HTTP contracts in `api-*` with `@GetExchange`/`@PostExchange`. Keep client
  paths such as `/role/authorizations` aligned with server routes such as `/inner/role/authorizations`.
- Rust changes follow Rust 2024 conventions and must pass `rustfmt`; preserve existing async, error,
  and tracing patterns.
- Frontend TypeScript and Vue changes follow existing patterns, Prettier, and ESLint. Run
  `bun run frontend:check` before committing frontend changes.
- Keep comments short and explain non-obvious decisions only. Avoid unrelated refactors.

## Commits

- Use Conventional Commits: `feat`, `fix`, `refactor`, `test`, `chore`, or `build`.
- Write an imperative, concise subject followed by a short body when needed.
- Do not add AI attribution or `Co-Authored-By` trailers. Claude Code attribution stays disabled in
  the ignored `.claude/settings.local.json` with empty `attribution.commit` and `attribution.pr`.
- Do not push or rewrite shared history unless explicitly requested.
