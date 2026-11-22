# AGENTS.md - Megalith Micro

Operating notes for AI coding agents working in this repository. Read [README.md](README.md) for
the complete architecture, application responsibilities, deployment model, and frontend SSR flow;
this file records the implementation constraints that are easy to miss.

## Toolchain

- Java 25 (GraalVM 25.5 Innovation HotSpot), Spring Boot 4.2.0, Hibernate ORM
  enhancer plugin 7.4.16.Final, Redisson 4.8.0, and Caffeine. Runtime dependencies use the
  Spring Boot BOM; the Hibernate plugin version is pinned separately in `build.gradle.kts`.
- Gradle 9.9.1 Kotlin DSL; the root `build.gradle.kts` configures all Java subprojects.
- Rust 2024 for `micro-gateway-rs`, `micro-sync-rs`, and `micro-search-rs`. Use the `-rs` suffix
  for Rust service directories and Cargo package names. Shared libraries under `crates/` omit it.
- Bun 1.4.5, Vue 3, and Vite for the standalone `micro-frontend` service; `tsrs` with the
  Vize content mapper checks TypeScript and Vue types. The frontend Docker builder uses Bun 1.4.5.

`JAVA_HOME` must point to a GraalVM HotSpot JDK, not the Espresso JVM. On macOS, for example:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-25.5.5.0.3+1.1/Contents/Home
```

## Checks and Commands

```bash
./gradlew build
./gradlew :micro-auth:test
# Separate Testcontainers suites; requires Docker, not included in build/check.
./gradlew :micro-auth:integrationTest :micro-user:integrationTest :micro-blog:integrationTest \
  :micro-exhibit:integrationTest :cache:integrationTest
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
| Applications | `micro-auth`, `micro-user`, `micro-blog`, `micro-exhibit` | Java native services |
| Rust applications | `micro-gateway-rs`, `micro-sync-rs`, `micro-search-rs` | Gateway, Redis-backed collaboration, and Meilisearch projections |
| Frontend | `micro-frontend` | Vue SSR, hydration, and embedded static assets |
| Java service contracts | `api-auth`, `api-user`, `api-blog` | Typed HTTP interfaces and RPC models |
| Shared Java | `common-*` | Contract, RPC, web, auth, observability, messaging, scheduling, outbox, export, shared layering rules |
| Cache | `cache` | Caffeine L1, Redis L2, and distributed eviction |

The frontend is an independent Bun workspace outside Gradle and Cargo. Dependency versions belong
in `micro-frontend/package.json` and the single root `bun.lock`; do not reintroduce a Bun
`catalog`, because Dependabot's `bun` ecosystem skips `catalog:` references.

## Architecture Invariants

1. **Single-pass authorization.** `micro-gateway-rs` calls `POST /inner/auth/route` once. The auth
   service returns the target host/port and a Base64URL principal in `X-Megalith-Principal`; business
   services trust that header and never re-derive identity from cookies.
2. **SSR and browser traffic are separate.** SSR prefetch uses `SSR_API_BASE_URL` through the gateway.
   Browser API and WebSocket traffic goes through nginx and the gateway. Access and refresh tokens
   stay in HttpOnly cookies; browser code must not read or persist them. SSR requests must create
   isolated Router, Pinia, i18n, head-management, and HTTP state.
3. **Stateless collaboration.** `micro-sync-rs` replicas coordinate through shared Redis Streams and
   state for documents, awareness, snapshots, presence, leases, and compaction. Do not add sticky
   session or room ownership assumptions.
4. **`@Cache` owns cache keys.** Only `micro-exhibit` and `micro-auth` may annotate business
   reads with `@Cache`. Data owners publish changes through their transactional outbox. The cache
   owner consumes those RabbitMQ events and uses `CacheEvictor` for exact L2 eviction and L1
   invalidation across replicas. Keys are
   `<namespace>:v<version>:<sha256(canonicalTypedArgs)>` from `CacheKeyFactory`. Cache methods must go
   through the Spring proxy. Reads are Caffeine L1 then Redis L2; misses execute the method and write
   both levels with the annotation TTL. Increase the explicit version for incompatible contracts.
5. **Eviction is exact and broadcast.** `AuthCacheKeys`/`CacheEvictor` use shared
   `CacheDescriptor` constants, acquire the same distributed key locks as reads, delete exact Redis
   keys, invalidate local L1, and broadcast through confirmed RabbitMQ fanout or a Redis reliable
   topic. There are no reflective method lookups in eviction.
6. **Ports and adapters.** Core Java code uses `domain`, `application.model`,
   `application.port.in`, `application.port.out`, `application.service`, `adapter.in.*`,
   `adapter.out.*`, and `config`. `domain` and `application` are framework-free and never name
   another service's `api-*` contract; services carry no Spring annotations and are declared as
   beans in `config`, which is the only place that knows every implementation. Adapters may use
   frameworks, but never depend on `config` or on the opposite adapter direction. `common-arch`
   defines these rules once and every application's layering test runs them. A paged use case
   returns the core `application.model.Page`; the adapter that renders the response builds the
   shared `common-contract` `PageAdapter` envelope from it. `*HttpServiceWrapper`
   classes unwrap `RemoteResult.requireSuccess(...)` behind directory/gateway ports. Spring Data
   repositories and JPA entities belong under `adapter.out.persistence`. Messaging adapters translate
   shared queue envelopes into application commands; `domain` and `application` never depend on the
   shared `common-contract` `message` package. Domain models do not depend on aggregate `*Snapshot`
   transport projections; adapters own those conversions. Shared enums, value objects, and application
   read projections remain allowed.
7. **Transaction boundary.** Services prepare inputs across reads without a transaction.
   Transactional persistence adapters (including existing `*Wrapper` classes) do only writes and
   the matching outbox insert in one short transaction and never query back. Do not add
   `@Transactional` to services or repository reads to transactional writers. `common-arch` identifies
   writable persistence transactions from method/class annotations and checks reachable Spring Data
   repository calls, including persistence helpers and method references, regardless of class names.
   Read each user-access, role-authorization, or blog-editor projection in one read-only
   `REPEATABLE_READ` persistence transaction. Initialize collaboration rooms after the editor read returns.
8. **Transactional outbox.** User and blog changes commit to `m_outbox_event` before confirmed
   RabbitMQ publication. Cache eviction and Meilisearch indexing consume those events. User
   deletion uses `UserDeletedMessage` on its dedicated fanout exchange; `micro-blog` consumes it to
   delete blogs owned by the deleted users. Never publish domain events outside the outbox.
9. **JPMS.** `cache` exports only its public `annotation`, `handler`, and `key` packages. A new
   public package needs an `exports` entry; downstream JPMS modules require `wiki.chiu.micro.cache`.
   Internals follow the application layout: `application` and `application.model` hold shared
   entries, payloads, lock names, and the metrics facade, `adapter.in.*` holds the read-through
   aspect and the eviction listeners, `adapter.out.*` holds key generation and the Redis and
   eviction adapters, and `config` holds auto-configuration, properties, conditions, contract
   validation, and the runtime hints registered through `META-INF/spring/aot.factories`.
10. **Native and AOT reachability.** Types used through reflection, serialization, HTTP interfaces, or
    native-image initialization need the matching Spring AOT/runtime hints. Declare HTTP payload
    binding hints in the application's `config/CustomRuntimeHints` with
    `BindingReflectionHintsRegistrar`; keep runtime-hint registration out of `adapter.in.http`.
    Shared third-party compatibility hints belong in `common-native-hints` under
    `wiki.chiu.micro.common.nativehints`, discovered through `META-INF/spring/aot.factories`.
    Keep cache-specific hints in `cache`. The shared hints artifact and cache starter publish
    together at the same `cacheVersion`; preserve that dependency in the starter's POM and JPMS descriptor.
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
    Inbound adapters call `RoomManager` rather than the concrete store. `micro-search-rs` keeps article
    projections in `domain`, query and rebuild orchestration in `application`, and HTTP, Redis,
    Meilisearch, and RabbitMQ in `adapter`. Declare Rust modules in same-name `.rs` files.
    Shared OTLP providers and W3C header propagation live in `crates/otel`. `micro-gateway-rs` remains a
    transport-oriented edge service: authentication belongs to `client`, no-I/O forwarding rules
    belong to `proxy`, and handlers/middleware coordinate them without an artificial domain layer.
15. **Common module packages.** Shared infrastructure modules use dedicated roots under
    `wiki.chiu.micro.common`, so `common-messaging` owns `wiki.chiu.micro.common.messaging`,
    `common-scheduling` owns `wiki.chiu.micro.common.scheduling`, and `common-outbox` owns
    `wiki.chiu.micro.common.outbox`. `common-outbox` mirrors the application layout with `domain`,
    `application`, `adapter.in.actuator`, `adapter.out.persistence.repository`, and `config`.
    `common-auth-web` uses `wiki.chiu.micro.common.auth.web`. `common-contract` groups contracts
    directly under `wiki.chiu.micro.common` by kind, one package per kind: `result` for response
    envelopes, `error` for error codes, `exception` for the runtime exception types that carry
    them, `message` for outbox payloads, `model` for cross-service projections, `enums` for shared
    domain enumerations, `constant` for shared constants, `page` for the paging envelope,
    `security` for the trusted principal and internal headers, and `validation` for reusable
    request validators. Every package carries a `package-info.java`; do not recreate the retired
    `wiki.chiu.micro.common.lang` bucket.
    `common-arch` owns `wiki.chiu.micro.common.arch` and holds the shared ArchUnit rules that define
    the application layout every service is verified against.

## Transaction review findings (2026-11-19)

The user and blog writers reviewed follow rules 7 and 8 for explicit repository reads and
outbox inserts. The following consistency issues remain open.

| Path | Finding |
| --- | --- |
| [RoleServiceImpl.saveOrUpdate](micro-user/src/main/java/wiki/chiu/micro/user/application/service/RoleServiceImpl.java) → [RoleWrapper](micro-user/src/main/java/wiki/chiu/micro/user/adapter/out/persistence/RoleWrapper.java) | A rename prepares the old and new role codes for eviction. The update matches only the role ID. Concurrent renames can omit the actual previous code from the later outbox event and leave its navigation cache stale. Include the expected old code in the write condition. |
| [MenuServiceImpl.delete](micro-user/src/main/java/wiki/chiu/micro/user/application/service/MenuServiceImpl.java) and [RoleDataPermissionServiceImpl.saveDataPermissions](micro-user/src/main/java/wiki/chiu/micro/user/application/service/RoleDataPermissionServiceImpl.java) | Menu deletion checks for children before the write. Permission replacement checks that the role exists before the write. Concurrent child creation or role deletion can leave orphan rows under the repository's entity mappings. The corresponding parent references are scalar IDs without mapped foreign keys. |
| [OutboxStoreAdapter.status](common-outbox/src/main/java/wiki/chiu/micro/common/outbox/adapter/out/persistence/OutboxStoreAdapter.java) | Separate READY and PAUSED counts can both return zero if a paused event resumes between reads. Blog index maintenance uses both counts to decide whether the outbox is empty. Compute them in one aggregate query or one repeatable-read snapshot. |
| [UserIdentityWrapper](micro-user/src/main/java/wiki/chiu/micro/user/adapter/out/persistence/UserIdentityWrapper.java) | Lock and unlock counters increment before commit. A rollback leaves the metrics counting changes that never committed. Record successful transitions after commit. |

## Code Style

- Review affected Java files individually. Put data conversion in dedicated `*Convertor` classes
  in the adapter that owns the boundary (requests/responses, RPC, messages, persistence entities,
  and search documents). Use the `convertor` package for persistence conversion. Do not scatter
  field copying across handlers, listeners, services, repositories, or domain models. Core-only
  model conversion belongs in framework-free `application.service.*Convertor` collaborators;
  preserve business decisions and orchestration in services. Do not introduce `*Mapper` or
  `*Converter` names for application data conversion. Framework SPI
  implementations such as Spring Security `AuthenticationConverter` retain framework naming.
  Convertors do not call repositories, remote services, or infrastructure stores, and do not own
  transactions. Request convertors may parse and validate the incoming request.

- Java uses 4-space indentation, sorted imports, no unused imports, and the surrounding method and
  comment order. Preserve Chinese Javadoc conventions in `micro-user`.
- In `micro-user` profile edits, a missing or empty password means to keep the existing password.
- A Java source file's physical directory must match its declared package in both `src/main/java`
  and `src/test/java`; Gradle may compile mismatches, but JDTLS reports them as errors.
- RPC models are records in `api-*`; preserve established hand-written builders such as
  `AuthorityRpcVo.builder()`.
- Nullability uses `org.jspecify` `@NonNull`. Jackson is Jackson 3's `tools.jackson.databind.JsonMapper`,
  not `com.fasterxml.jackson`.
- Server WebMVC is functional: use `*Handler` with `RouterFunctions`, keep internal routes under
  `/inner/...`, and declare Java service contracts in `api-*` with `@GetExchange`/`@PostExchange`.
  The Rust search service owns its contract. Its Java clients declare their HTTP interfaces and
  payload records under `adapter.out.http.search`. Keep client
  paths such as `/role/authorizations` aligned with server routes such as `/inner/role/authorizations`.
- Rust changes follow Rust 2024 conventions and must pass `rustfmt`; preserve existing async, error,
  and tracing patterns.
- Frontend TypeScript and Vue changes follow existing patterns, Oxlint, and Oxfmt. Run
  `bun run frontend:check` before committing frontend changes. `tsrs` checks the app, Vitest, and
  Bun projects; app/Vitest use the Vize `.vue` content mapper with `--runExternalCode`. Launch
  through `bun --bun tsrs` so mapper subprocesses also use Bun in the build image. Keep the
  compiler and mapper versions pinned, and run `bun run --filter megalith-frontend test:type-check`
  when upgrading either. Do not add a wildcard `*.vue` module declaration that erases component
  props or hides missing mapper coverage. Keep `verify` as the full-check script name.
  Run lint through the package script: it selects the workspace `tsrs-lint` backend, enables
  external Vue mappers, and denies warnings. CI uses the same lockfile-installed Oxlint. Its
  type-aware rules cover TypeScript files; Oxlint currently skips `.vue` for type-aware linting.
  Run `test:lint` when changing the lint backend or configuration.
- Keep comments short and explain non-obvious decisions only. Avoid unrelated refactors.

## Commits

- Use Conventional Commits: `feat`, `fix`, `refactor`, `test`, `chore`, or `build`.
- Write an imperative, concise subject followed by a short body when needed.
- Do not add AI attribution or `Co-Authored-By` trailers. Claude Code attribution stays disabled in
  the ignored `.claude/settings.local.json` with empty `attribution.commit` and `attribution.pr`.
- Do not push or rewrite shared history unless explicitly requested.
