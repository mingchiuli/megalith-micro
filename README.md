# Megalith Micro

[![Java Version](https://img.shields.io/badge/Java-24+-orange.svg)](https://openjdk.java.net/)
[![Rust Version](https://img.shields.io/badge/Rust-2024-edb974.svg)](https://www.rust-lang.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A hybrid Java/Rust microservices platform providing multi-level caching, distributed tracing, and real-time collaboration capabilities.

## 🏗️ Architecture

```mermaid
graph TD
    %% Layer Definitions
    subgraph ExternalLayer[External Layer]
        Nginx[nginx<br/>External Gateway + Frontend Proxy]
    end

    subgraph GatewayLayer[Gateway Layer]
        Gateway[gateway service (Rust)<br/>Request Auth & Routing]
    end

    subgraph ServiceLayer[Service Layer]
        Auth[auth service<br/>Permission L2 Cache / Login API + Cache Update]
        User[user service<br/>User & Permission Management]
        Blog[blog service<br/>Blog Content Management]
        Sync[sync service (Rust)<br/>Collaborative Editing WS (Single Point)]
        Exhibit[exhibit service<br/>Blog L2 Cache]
        Search[search service<br/>Search Functionality]
    end

    subgraph StorageLayer[Storage & Middleware Layer]
        MariaDB[mariadb<br/>User/Blog Storage]
        Redis[redis<br/>Distributed Cache]
        RabbitMQ[rabbitmq<br/>Message Queue]
        ES[ElasticSearch<br/>Search/APM Storage]
    end

    subgraph MonitoringLayer[Monitoring Layer]
        APM-Server[apm-server<br/>OTel Data Receiver]
        Kibana[kibana<br/>Monitoring Visualization]
    end

    %% Traffic Flow (Sync uses WS, others HTTP)
    Nginx --> Gateway
    Gateway -->|HTTP/WS: Auth Call, Route Selection| Auth
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
    Blog -->|Complex Query: ES Query ID then Fetch| Search

    %% Messages & Cache (Auth receives RabbitMQ messages to update cache)
    User -->|Message| RabbitMQ
    Blog -->|Message| RabbitMQ
    RabbitMQ -->|Update Cache| Exhibit
    RabbitMQ -->|Update ES| Search
    RabbitMQ -->|Update Cache| Auth
    Exhibit -->|L1 Cache| Redis
    Auth -->|L1 Cache| Redis

    %% Monitoring Flow
    User & Blog & Auth & Exhibit & Search & Sync & Gateway -->|OTel Data| APM-Server
    APM-Server --> ES
    ES --> Kibana
```

## 📦 Modules

### Java Modules (Spring Boot)

| Module | Description |
|--------|-------------|
| `cache` | Megalith Cache Spring Boot Starter - Multi-level caching (L1 Caffeine + L2 Redis) with distributed eviction |
| `common` | Shared utilities, DTOs, and converters |
| `micro-auth` | Authentication service - JWT, Email/SMS authentication |
| `micro-blog` | Blog content management with sensitive content handling |
| `micro-user` | User management with Hibernate ORM |
| `micro-exhibit` | Blog display and visit statistics |
| `micro-search` | Full-text search powered by Elasticsearch |

### Rust Modules

| Module | Description |
|--------|-------------|
| `micro-gateway-rs` | High-performance API gateway built with Axum, JWT auth, OpenTelemetry tracing |
| `micro-sync-rs` | Real-time collaboration sync service using WebSocket and YRS CRDT |

## ✨ Features

- **Multi-Level Caching**: L1 (Caffeine) + L2 (Redis) with automatic eviction via RabbitMQ or Redis pub/sub
- **Distributed Tracing**: Full OpenTelemetry integration across all services
- **GraalVM Native Support**: All Java microservices support native compilation
- **Real-time Collaboration**: CRDT-based sync via WebSocket (YRS)
- **JWT Authentication**: Secure token-based auth across services
- **JPMS Module**: Proper Java Platform Module System support

## 🛠️ Tech Stack

**Java:**
- Spring Boot 4.0.5
- Hibernate ORM 7.3.0
- Redisson 4.3.1 (Redis)
- Caffeine Cache
- Elasticsearch

**Rust:**
- Axum 0.8 (Web framework)
- Tokio (Async runtime)
- YRS (CRDT)
- OpenTelemetry

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

- JDK 24+
- Rust 2024 edition
- Redis
- RabbitMQ (optional, for distributed cache eviction)

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

## 📁 Project Structure

```
megalith-micro/
├── cache/                    # Cache Spring Boot Starter
├── common/                   # Shared utilities
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
