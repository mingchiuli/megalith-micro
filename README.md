# Megalith Micro

[![Java Version](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.java.net/)
[![Rust Version](https://img.shields.io/badge/Rust-2024-edb974.svg)](https://www.rust-lang.org/)
[![Node.js Version](https://img.shields.io/badge/Node.js-24-5fa04e.svg)](https://nodejs.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A Java, Rust, and Node.js microservices platform providing multi-level caching, distributed tracing, server-side rendering, and real-time collaboration capabilities.

## 🏗️ Architecture

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
        Frontend["megalith-frontend separate repository<br/>Node.js + Express + Vue 3 SSR<br/>Static Assets + Server Prefetch"]
    end
    subgraph GatewayLayer[Gateway Layer]
        Gateway["gateway service Rust<br/>Origin Check + Single Auth/Route Resolution<br/>HTTP and WebSocket Proxy"]
    end
    subgraph ServiceLayer[Service Layer]
        Auth["auth service<br/>Permission L2 Cache / Login API + Cache Update"]
        User["user service<br/>User & Permission Management"]
        Blog["blog service<br/>Blog Content Management"]
        Sync["sync service Rust replicas<br/>Stateless Collaborative Editing WS"]
        Exhibit["exhibit service<br/>Blog L2 Cache"]
        Search["search service<br/>Search Functionality"]
    end
    subgraph StorageLayer[Storage & Middleware Layer]
        MariaDB["mariadb<br/>User/Blog Storage"]
        Redis["redis<br/>Distributed Cache"]
        RabbitMQ["rabbitmq<br/>Message Queue"]
        ES["ElasticSearch<br/>Search/APM Storage"]
    end
    subgraph MonitoringLayer[Monitoring Layer - Server]
        APMServer["apm-server<br/>OTel Data Receiver"]
    end
    subgraph LocalMachine[Local Machine - Developer Laptop]
        Kibana["kibana<br/>Monitoring Visualization<br/>(Local Install)"]
    end
    %% Traffic Flow: page requests use SSR; browser API calls bypass Node
    Browser -->|Page / Asset / API / WS Requests| Nginx
    Nginx -->|Page Routes + Static Assets| Frontend
    Nginx -->|/api HTTP + /wsapi WS| Gateway
    Frontend -->|SSR Prefetch HTTP<br/>Docker Internal Network| Gateway
    Gateway -->|Single HTTP/WS Authorize + Route Resolution| Auth
    Gateway -->|HTTP| User
    Gateway -->|HTTP| Blog
    Gateway -->|WS| Sync
    Gateway -->|HTTP| Exhibit
    Gateway -->|HTTP| Search
    %% Business Dependencies
    User --> MariaDB
    Blog --> MariaDB
    Exhibit -->|Fetch Data| User
    Exhibit -->|Fetch Data| Blog
    Auth -->|Fetch Data| User
    Search --> ES
    Blog -->|Complex Query ES Query ID then Fetch| Search
    %% Messages & Cache Auth receives RabbitMQ messages to update cache
    User -->|Message| RabbitMQ
    Blog -->|Message| RabbitMQ
    RabbitMQ -->|Update Cache| Exhibit
    RabbitMQ -->|Update ES| Search
    RabbitMQ -->|Update Cache| Auth
    Exhibit -->|L2 Cache| Redis
    Auth -->|L2 Cache| Redis
    Sync -->|Session Streams / Snapshots / Presence| Redis
    %% Monitoring Flow
    Frontend & User & Blog & Auth & Exhibit & Search & Sync & Gateway -->|OTel Traces / Metrics / Logs| APMServer
    APMServer --> ES
    ES -->|Encrypted WireGuard VPN| Kibana
```

## 📦 Modules

### Java Modules (Spring Boot)

| Module | Description |
|--------|-------------|
| `cache` | Megalith Cache Spring Boot Starter - Multi-level caching (L1 Caffeine + L2 Redis) with distributed eviction |
| `auth-api`, `user-api`, `blog-api`, `search-api` | Typed HTTP service contracts and RPC request/response models |
| `common-contract` | Shared result, error, paging, validation, and security contracts |
| `common-rpc` | HTTP client groups, authentication propagation, and external storage/SMS clients |
| `common-web`, `common-auth-web` | Functional WebMVC support, validation, error handling, and authenticated principals |
| `common-observability` | OpenTelemetry propagation, logging, metrics, and runtime hints |
| `common-export` | Shared export utilities |
| `micro-auth` | Authentication service - JWT, Email/SMS authentication |
| `micro-blog` | Blog content management with sensitive content handling |
| `micro-user` | User management with Hibernate ORM |
| `micro-exhibit` | Blog display and visit statistics |
| `micro-search` | Full-text search powered by Elasticsearch |

### Rust Modules

| Module | Description |
|--------|-------------|
| `micro-gateway-rs` | High-performance Axum gateway with centralized authorization, dynamic routing, and OpenTelemetry tracing |
| `micro-sync-rs` | Real-time collaboration sync service using WebSocket and YRS CRDT |

### Related Repository

[`megalith-frontend`](https://github.com/mingchiuli/megalith-frontend) is the separately
versioned Vue 3 + Vite SSR application shown in the architecture diagram. Its Node.js runtime
renders public and administration routes, prefetches initial data through this repository's
gateway, and hydrates the application in the browser.

## ✨ Features

- **Multi-Level Caching**: L1 (Caffeine) + L2 (Redis) with automatic eviction via RabbitMQ or Redis pub/sub
- **Distributed Tracing**: Full OpenTelemetry integration across all services
- **GraalVM Native Support**: All Java microservices support native compilation
- **Real-time Collaboration**: CRDT-based sync via WebSocket (YRS)
- **Stateless Sync Replicas**: Collaboration sessions are relayed and compacted through shared Redis; load balancers do not need sticky sessions
- **Single-Pass Gateway Authorization**: Auth validates the method/path and token, then returns the target service in one synchronous call
- **Method-Preserving Proxy**: The Rust gateway forwards the original HTTP method, query, body, response status, and response body, including DELETE and PATCH requests
- **JWT Authentication**: Secure token-based auth across services
- **JPMS Cache Module**: The reusable cache starter exports only its public API packages

## Gateway Request Flow

1. nginx sends `/api` HTTP requests and `/wsapi` WebSocket upgrades to `micro-gateway-rs`.
2. The gateway validates the request origin when the request carries credentials or uses an unsafe method.
3. The gateway calls `POST /inner/auth/route` once with the original HTTP method, path, client IP, and credential.
4. `micro-auth` selects the most specific registered route, validates whitelist or user/role access, and returns the target host and port only when authorized.
5. The gateway forwards the request with its original method, query, and body. WebSocket upgrades use the same resolved route and do not call auth again.

Only method/path pairs registered in the authority data can be routed. The gateway fails closed
when auth is unavailable. Deploy `micro-auth` and `micro-gateway-rs` together in a coordinated
maintenance window when this internal route contract changes; external browser and frontend URLs
remain unchanged.

## 🛠️ Tech Stack

**Java:**
- Spring Boot 4.1.0
- Hibernate ORM 7.4.5.Final
- Redisson 4.7.0 (Redis)
- Caffeine Cache
- Elasticsearch

**Rust:**
- Axum 0.8 (Web framework)
- Tokio (Async runtime)
- YRS (CRDT)
- OpenTelemetry

**Node.js:**
- Express 5 + Vue 3 SSR
- OpenTelemetry traces, metrics, and logs

**Infrastructure:**
- Nginx (External Gateway + Frontend Proxy)
- MariaDB (User/Blog Storage)
- Redis (Distributed Cache)
- RabbitMQ (Message Queue)
- ElasticSearch (Search + APM Storage)
- APM-Server (OTel Data Receiver)
- Kibana (Monitoring Visualization)

## 🚀 Quick Start

### Prerequisites

- JDK 25
- Rust 2024 edition
- Redis
- RabbitMQ (required for domain events, search indexing, and the default distributed cache eviction path)
- Elasticsearch

### Build

```bash
# Build all Java modules
./gradlew build

# Build Rust modules
cargo build --manifest-path micro-gateway-rs/Cargo.toml
cargo build --manifest-path micro-sync-rs/Cargo.toml
```

### Run

```bash
# Run Java microservices
./gradlew :micro-auth:bootRun
./gradlew :micro-blog:bootRun

# Run Rust services
cargo run --manifest-path micro-gateway-rs/Cargo.toml
cargo run --manifest-path micro-sync-rs/Cargo.toml
```

`micro-sync-rs` requires Redis 6.2 or newer. Every replica must use the same
single-node Redis endpoint through `REDIS_URL`; Redis Cluster is intentionally
not supported. Redis stores the transient collaboration draft for five minutes
after the final connection lease expires. Explicit saves continue to use the
blog service and MariaDB as the source of truth.

Nested sync settings use a double underscore in environment variables, for
example `SYNC__SESSION_RETENTION_SECONDS=300` and `WORKER__CONCURRENCY=4`.

The `cache` starter can use Redis Pub/Sub for cache eviction when Spring AMQP is absent. The
deployed platform still requires RabbitMQ for blog/user domain events and Elasticsearch indexing.

## 📁 Project Structure

```
megalith-micro/
├── auth-api/                 # Authentication HTTP contract
├── blog-api/                 # Blog HTTP contract
├── search-api/               # Search HTTP contract
├── user-api/                 # User HTTP contract
├── common-contract/          # Shared wire and error contracts
├── common-rpc/               # HTTP clients and remote adapters
├── common-web/               # Functional WebMVC support
├── common-auth-web/          # Authenticated web support
├── common-observability/     # OpenTelemetry integration
├── common-export/            # Export utilities
├── cache/                    # Multi-level cache starter
├── micro-auth/               # Authentication service
├── micro-blog/               # Blog service
├── micro-exhibit/            # Blog display service
├── micro-search/             # Search service
├── micro-user/               # User service
├── micro-gateway-rs/         # Rust API Gateway
└── micro-sync-rs/            # Rust Sync Service
```

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
