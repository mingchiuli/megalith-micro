# Megalith Micro

[![GraalVM](https://img.shields.io/badge/GraalVM-Java%2025-f29111.svg)](https://www.graalvm.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6db33f.svg)](https://spring.io/projects/spring-boot)
[![Rust](https://img.shields.io/badge/Rust-2024-000000.svg)](https://www.rust-lang.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

Megalith Micro is a platform monorepo designed to ship every application as a **single native
executable**.

**The Java services are not deployed as JVM applications or JARs.** All five Spring Boot services
use Java 25 and GraalVM Native Image for ahead-of-time compilation, so their production images do
not require a JVM or JRE. The gateway and collaboration service compile to native Rust binaries;
the frontend service is compiled to a standalone executable by Bun.

The entire platform therefore follows the same model: **one application, one native executable,
one independent OCI image**. Application images contain the executable and only the minimal
runtime files, with no source code, build toolchain, or separate language runtime.

| Applications | Technology | Production artifact |
| --- | --- | --- |
| `micro-auth`, `micro-user`, `micro-blog`, `micro-exhibit`, `micro-search` | Java 25, Spring Boot, GraalVM | GraalVM Native Image executable |
| `micro-gateway-rs`, `micro-sync-rs` | Rust 2024, Tokio, Axum | Rust release executable |
| [`micro-frontend`](micro-frontend/docs/ssr-architecture.md) | Bun 1.4, Vue 3.5, Vite 8, SSR | Bun standalone executable with embedded assets |

> MariaDB, Redis, RabbitMQ, Elasticsearch, and other infrastructure components continue to use
> their standard images. "Single binary" describes how the platform applications are built and
> delivered.

## Architecture

```mermaid
graph TD
    %% Layer Definitions
    subgraph ClientLayer[Client Layer]
        Browser["Browser<br/>Vue 3 Hydrated Client"]
    end
    subgraph ExternalLayer[External Layer]
        Nginx["nginx<br/>Reverse Proxy"]
    end
    subgraph FrontendLayer[Frontend Layer]
        Frontend["micro-frontend - Bun Standalone Executable<br/>Vue 3 SSR + Embedded Static Assets<br/>Server Prefetch + Client Hydration"]
    end
    subgraph GatewayLayer[Gateway Layer]
        Gateway["micro-gateway-rs - Native Rust Binary<br/>Origin Check + Single Auth/Route Resolution<br/>Pooled Streaming HTTP / WebSocket Proxy"]
    end
    subgraph ServiceLayer[Service Layer - Native Executables]
        Auth["micro-auth - GraalVM Native Image<br/>Route and Role Permission Cache<br/>Login API + Principal Resolution"]
        User["micro-user - GraalVM Native Image<br/>User and Permission Management"]
        Blog["micro-blog - GraalVM Native Image<br/>Blog Content Management"]
        Sync["micro-sync-rs - Native Rust Binary<br/>Stateless Collaborative Editing WebSocket"]
        Exhibit["micro-exhibit - GraalVM Native Image<br/>Content Presentation + L2 Cache"]
        Search["micro-search - GraalVM Native Image<br/>Full-Text Search + Index Consumer"]
    end
    subgraph StorageLayer[Storage and Middleware Layer]
        MariaDB["MariaDB<br/>User / Blog Storage"]
        Redis["Redis<br/>Distributed Cache + Sync State"]
        RabbitMQ["RabbitMQ<br/>Domain Events"]
        ES["Elasticsearch<br/>Search + APM Storage"]
    end
    subgraph MonitoringLayer[Monitoring Layer - Server]
        APMServer["APM Server<br/>OpenTelemetry Receiver"]
    end
    subgraph LocalMachine[Local Machine - Developer]
        Kibana["Kibana<br/>Monitoring Visualization"]
    end

    %% Page requests use SSR; browser API calls bypass the frontend server.
    Browser -->|Page / Asset / API / WS Requests| Nginx
    Nginx -->|Page Routes + Static Assets| Frontend
    Nginx -->|/api HTTP + /wsapi WS| Gateway
    Frontend -->|SSR Prefetch HTTP<br/>Internal Network| Gateway
    Gateway -->|Single HTTP/WS Auth + Route Resolution| Auth
    Gateway -->|HTTP| User
    Gateway -->|HTTP| Blog
    Gateway -->|WS| Sync
    Gateway -->|HTTP| Exhibit
    Gateway -->|HTTP| Search

    %% Business dependencies.
    User --> MariaDB
    Blog --> MariaDB
    Exhibit -->|Fetch Data| User
    Exhibit -->|Fetch Data| Blog
    Auth -->|Batch Snapshot Misses| User
    Search --> ES
    Blog -->|Query IDs, Then Fetch| Search

    %% Durable events and distributed caches.
    User -->|Transactional Outbox| MariaDB
    Blog -->|Transactional Outbox| MariaDB
    MariaDB -->|Confirmed Publish| RabbitMQ
    RabbitMQ -->|Invalidate Auth Cache| Auth
    RabbitMQ -->|Invalidate Exhibit Cache| Exhibit
    RabbitMQ -->|Update Search Index| Search
    Auth -->|L2 Cache| Redis
    Exhibit -->|L2 Cache| Redis
    Sync <-->|Replica Coordination<br/>Event Streams / Snapshots<br/>Presence / Leases / Compaction| Redis

    %% Observability.
    Frontend & User & Blog & Auth & Exhibit & Search & Sync & Gateway -->|OTel Traces / Metrics / Logs| APMServer
    APMServer --> ES
    ES -->|Encrypted WireGuard VPN| Kibana
```

All external traffic enters through nginx. The Rust gateway proxies HTTP and WebSocket requests,
calls `micro-auth` once for authorization and route resolution, and passes a trusted principal to
the target service. Business services never re-parse browser credentials.

`micro-sync-rs` replicas coordinate through Redis. Document and awareness updates are appended to
shared Redis Streams and relayed to connections on every replica, while snapshots, presence
ownership, connection leases, and compaction work remain in shared Redis state. Any replica can
therefore accept a connection for any room without sticky sessions or assigning that room to a
single process.

## Applications and Modules

### Deployable Applications

| Application | Responsibility |
| --- | --- |
| `micro-gateway-rs` | Streaming HTTP/WebSocket proxy, origin checks, centralized authorization entry point, and dynamic routing |
| `micro-auth` | JWT, login, route authorization, and authorization snapshot caches |
| `micro-user` | Users, roles, menus, authorities, and data permissions |
| `micro-blog` | Blog content, recycle bin, and domain events |
| `micro-exhibit` | Content presentation, visit statistics, and presentation caches |
| `micro-search` | Elasticsearch full-text search and index event consumption |
| `micro-sync-rs` | Stateless real-time collaboration backed by YRS CRDT and Redis |
| `micro-frontend` | Vue 3 SSR, server prefetch, client hydration, and embedded static assets |

### Shared Java Modules

| Module | Responsibility |
| --- | --- |
| `api-auth`, `api-user`, `api-blog`, `api-search` | Typed HTTP contracts and RPC records |
| `cache` | Caffeine L1 + Redis L2 caching, exact eviction, and replica-wide invalidation |
| `common-contract` | Result, error, paging, validation, and security contracts |
| `common-rpc` | HTTP clients, principal propagation, and external service adapters |
| `common-web`, `common-auth-web` | Functional WebMVC, error handling, validation, and trusted principal resolution |
| `common-observability` | OpenTelemetry integration and GraalVM runtime hints |
| `common-messaging`, `common-outbox` | Consumer retries, dead-letter queues, and the transactional outbox |
| `common-scheduling`, `common-export` | Distributed scheduler locks and export utilities |

## Frontend

`micro-frontend` is the Bun workspace for the production `megalith-frontend` service. JavaScript
dependency versions are defined once in the root `package.json` catalog, while the workspace
declares the packages it uses through the `catalog:` protocol. The service remains independently
built and deployed outside the Gradle and Cargo workspaces.

Public and administration routes are rendered on the Bun server and hydrated by Vue in the
browser. Each SSR request creates isolated Vue Router, Pinia, i18n, head-management, and HTTP
state; route data prefetched through `micro-gateway-rs` is serialized into the page and reused
during hydration. Subsequent browser API and WebSocket traffic goes directly through nginx to the
gateway instead of passing through the frontend server.

Authentication tokens are transported only in HttpOnly cookies. The SSR server forwards request
cookies to the gateway and propagates refreshed `Set-Cookie` headers, while browser code never
reads or persists access or refresh tokens.

Vite builds the client and SSR bundles, then Bun compiles the server, runtime, and assets into
`micro-frontend/dist/bin/megalith-frontend`. The runtime image contains only that executable and
minimal OS runtime files. It exposes `/actuator/health`, performs graceful shutdown, and exports
correlated OpenTelemetry traces, metrics, and logs.

| Variable | Default / production value | Purpose |
| --- | --- | --- |
| `PORT` | `1919` | Bun SSR listen port |
| `SSR_API_BASE_URL` | `http://127.0.0.1:8088` / `http://micro-gateway-rs:8088` | Gateway used for SSR prefetch |
| `APP_ORIGIN` | Incoming request origin | Origin forwarded for cookie-authenticated requests |
| `OTEL_SERVICE_VERSION` | Package version / deployed Git SHA | Frontend release identifier |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://127.0.0.1:8200` / `http://apm-server:8200` | Base OTLP endpoint for traces, metrics, and logs |
| `LOG_LEVEL` | `info` | Console and OTLP application log threshold |

See the [SSR architecture](micro-frontend/docs/ssr-architecture.md) for request flow,
authentication, caching, observability, failure behavior, and deployment details.

## Core Design

- **Single-pass authorization:** the gateway calls `POST /inner/auth/route` once to resolve both
  the target service and trusted principal. Business services trust only the
  `X-Megalith-Principal` injected by the gateway.
- **Two-level caching:** `@Cache` owns cache key generation and reads through Caffeine L1 followed
  by Redis L2. Eviction events are broadcast to every replica to clear local entries.
- **Short write transactions:** services perform reads, validation, and input preparation outside
  a transaction. Wrappers perform only writes and the matching outbox insert inside one short
  transaction. ArchUnit enforces this boundary.
- **Reliable domain events:** user and blog changes commit to a MariaDB transactional outbox before
  confirmed publication through RabbitMQ. Cache eviction and Elasticsearch indexing consume these
  durable events.
- **Stateless collaboration:** `micro-sync-rs` uses YRS CRDT and shared Redis for session streams,
  snapshots, and presence. Replicas do not require sticky sessions.
- **Native observability:** Java Native Image, Rust, and Bun applications export OpenTelemetry
  traces, metrics, and logs.
- **Centralized workspace versions:** the root Bun catalog owns JavaScript dependency versions;
  each frontend declares only the packages it uses through the `catalog:` protocol.

## Build

### Requirements

- GraalVM for JDK 25 using the HotSpot JVM, not the Espresso JVM
- Gradle 9.7 through the included Wrapper
- Rust stable with 2024 edition support
- Bun 1.4.0
- Docker for OCI image builds

Example on macOS:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-25.2.4+7.1/Contents/Home
```

### Checks and Tests

```bash
./gradlew build
cargo fmt --all -- --check
cargo clippy --workspace --all-targets -- -D warnings
cargo test --workspace
bun install --frozen-lockfile
bun run frontend:check
```

The Java build includes unit tests, ArchUnit, and Spring AOT test processing.

### Native Executables

```bash
# Java / GraalVM Native Image
./gradlew :micro-auth:nativeCompile

# Rust
cargo build --workspace --release

# Bun workspace / frontend
bun run frontend:build
```

Java artifacts are written to each module's `build/native/nativeCompile/` directory. Rust artifacts
are written to `target/release/`; the frontend executable is written to
`micro-frontend/dist/bin/megalith-frontend`.

### Application Images

Spring Boot Buildpacks compile and publish each Java service as a GraalVM Native Image. Set
`DOCKER_USERNAME` and `DOCKER_PWD` before running the task:

```bash
./gradlew :micro-auth:bootBuildImage
```

The Rust and Bun services use multi-stage Dockerfiles. Their final images contain only the release
executable and required runtime files:

```bash
docker build -t megalith-micro-gateway-rs:latest -f micro-gateway-rs/Dockerfile .
docker build -t megalith-micro-sync-rs:latest -f micro-sync-rs/Dockerfile .
docker build -t mingchiuli/megalith-frontend:latest -f micro-frontend/Dockerfile .
```

CI validates AOT processing and builds a separate GraalVM Native Image for each of the five Java
services. It formats, lints, and tests the two Rust services and the Bun frontend before building
their release images. Changed services are published independently and deployed in platform order,
with the frontend after the gateway.

### Development

Native compilation can be skipped when running a single service during development:

```bash
./gradlew :micro-auth:bootRun
cargo run -p micro-gateway-rs
bun run frontend:dev
```

The frontend development server listens on `http://127.0.0.1:1919` and expects the gateway at
`http://127.0.0.1:8088`. For local HTTP login, run `micro-auth` with
`MEGALITH_AUTH_COOKIE_SECURE=false`.

A complete local deployment also requires MariaDB, Redis, RabbitMQ, and Elasticsearch. Connection,
port, and OpenTelemetry settings are defined in each module's `application.yml`.

## License

This project is licensed under the [Apache License 2.0](LICENSE).
