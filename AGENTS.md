# AGENTS.md — Megalith Micro

Operating notes for AI coding agents working in this repository. Read the
[README.md](README.md) for the full architecture diagram and feature list; this file
records the non-obvious environment, command, and code-architecture facts an agent needs
to work correctly without breaking the project's invariants.

## Toolchain

- **Java 25 (GraalVM)**, Spring Boot 4.1.0, Hibernate ORM 7.4.5, Redisson 4.7.0, Caffeine.
- **Gradle 9.7** (Kotlin DSL), root `build.gradle.kts` configures all subprojects.
- **Rust 2024 edition** for `micro-gateway-rs` and `micro-sync-rs`.
- **Bun 1.4.0** workspace at the repository root; Vue 3 and Vite for the SSR frontend in
  `micro-frontend`.

> ⚠️ **JAVA_HOME must be a GraalVM HotSpot JDK, not the Espresso JVM.**
> `espresso-java25` (which macOS may select by default) makes Gradle configuration crash with a
> `StackOverflow` during `createJavaToolchainResolverRegistry`. Use e.g.
> `export JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-25.2.4+7.1/Contents/Home`
> before running any Gradle command.

## Common Commands

```bash
./gradlew build                      # full Java build: tests + AOT test compile
./gradlew :micro-auth:test           # test one module (replace module name)
./gradlew compileJava compileTestJava
./gradlew :micro-auth:bootRun        # run a service
cargo build --manifest-path micro-gateway-rs/Cargo.toml
bun install --frozen-lockfile
bun run frontend:check
```

- Every `micro-*` module also runs **ArchUnit** tests and Spring AOT test compilation
  (`processTestAot`) during `test` — keep AOT reflection hints (`@RegisterReflectionForBinding`,
  `CustomRuntimeHints`) in sync when you add types touched by reflection.

## Module Map

| Module | Purpose |
|--------|---------|
| `cache` | JPMS **cache starter**: `@Cache` aspect, L1 Caffeine + L2 Redis, distributed eviction |
| `api-*` (`api-auth`, `api-user`, `api-blog`, `api-search`) | Typed HTTP interface contracts + RPC VOs (records) |
| `common-*` | Contract, rpc, web, auth-web, observability, messaging, scheduling, outbox, export |
| `micro-auth` | JWT / route authorization; owns the auth snapshot caches |
| `micro-user` | User & permission management (Hibernate) |
| `micro-blog` / `micro-exhibit` / `micro-search` | Content, display/visit stats, search |
| `micro-gateway-rs` / `micro-sync-rs` | Gateway proxy and Redis-backed collaborative editing |
| `micro-frontend` | Bun standalone service: Vue 3 SSR, hydration, and embedded static assets |

## Architecture Invariants

1. **Gateway authorization is single-pass.** `micro-gateway-rs` calls `POST /inner/auth/route`
   once; `micro-auth` returns the target host/port and a Base64URL principal (`X-Megalith-Principal`).
   Business services trust that header — never re-derive identity from cookies.
2. **`@Cache` owns cache keys.** Key = `{prefix}:{SimpleClassName}:{methodName}[:serializedArg...]`
   via `CommonCacheKeyGenerator`. `@Cache` methods must go through the Spring proxy (self-invocation
   bypasses the aspect). Cache reads are Caffeine L1 → Redis L2; miss runs the method body and writes
   both with the annotation TTL.
3. **Eviction is by exact key + broadcast.** `AuthCacheKeys`/`CacheEvictHandler` delete Redis keys
   and fan out to every replica (RabbitMQ fanout, or Redis pub/sub when AMQP is absent) to invalidate
   L1 Caffeine. If you rename/remove a `@Cache` method, update the reflective `getMethod(...)` lookups
   in `AuthCacheKeys` and any `api-*` contract — a stale lookup breaks eviction or the auth listener.
4. **Port / wrapper pattern.** Services depend on ports (e.g. `UserDirectory`); concrete
   `*Wrapper`/`*HttpServiceWrapper` classes adapt to remote HTTP interfaces. Wrappers unwrap
   `RemoteResult.requireSuccess(...)`.
5. **Transaction boundary.** Services prepare inputs across many reads **without** a transaction;
   `*Wrapper` classes perform only writes + outbox insert in one short transaction and never query
   back. ArchUnit enforces both directions — do not add `@Transactional` to services or repository
   reads to wrappers.
6. **Transactional outbox.** `micro-user`/`micro-blog` commit domain events to `m_outbox_event`
   (producer-discriminated) and a shared scheduler publishes to RabbitMQ with confirms + retry.
   Cache eviction and ES indexing consume those events. Never publish an event outside the outbox.
7. **JPMS.** `cache` is a JPMS module (`module-info.java`) exporting only its public API packages
   (`annotation`, `handler`, `utils`). Adding a public class requires an `exports` line; downstream
   JPMS modules must `requires wiki.chiu.micro.cache`.
8. **GraalVM native / AOT.** All `micro-*` modules compile to native images. Types reachable via
   reflection, HTTP interfaces, or serialization need AOT hints.
9. **Bun workspace, independent frontend service.** Root `package.json` catalogs all JavaScript
   dependency versions and `bun.lock` pins their resolution; workspace packages declare usage with
   `catalog:`. Run install and `frontend:*` scripts from the repository root. The frontend remains
   outside Gradle/Cargo and deploys independently. Browser API/WebSocket traffic goes through nginx
   and `micro-gateway-rs`; SSR prefetch uses `SSR_API_BASE_URL` internally.

## Code Style

- Java uses 4-space indentation. Keep imports sorted and unused imports removed, and match the
  surrounding style exactly (method order, comment density, Chinese javadoc in `micro-user`).
- RPC models are **records** in `api-*` modules; some VOs keep a hand-written builder
  (`AuthorityRpcVo.builder()`).
- Nullability uses **org.jspecify** `@NonNull`. Jackson mapper is the `tools.jackson.databind`
  (`JsonMapper`) from Jackson 3 — not `com.fasterxml.jackson`.
- Functional WebMVC on the server side: `*Handler` + `RouterFunctions` routes (internal routes under
  `/inner/...`); HTTP interface annotations (`@GetExchange`/`@PostExchange`) in the `api-*` module.
  Path conventions: client `@GetExchange("/role/authorizations")` ↔ server route
  `/inner/role/authorizations`.
- Frontend TypeScript and Vue files use ESLint and Prettier. Run `bun run frontend:check` from the
  repository root before committing frontend changes.

## Committing

- Conventional Commits, matching repo history: `feat`, `fix`, `refactor`, `test`, `chore`, `build`.
- Write in the imperative mood, one short subject line, then a body paragraph.
- Do not add AI attribution or co-author trailers to commit messages.

- Do not push unless asked; the branch may be intentionally ahead of `origin/main`.
