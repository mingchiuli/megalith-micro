ARG BASE_IMAGE=debian:bookworm-slim

FROM ${BASE_IMAGE} AS base

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl tzdata \
    && rm -rf /var/lib/apt/lists/*

FROM base AS builder

ENV CARGO_HOME=/usr/local/cargo \
    RUSTUP_HOME=/usr/local/rustup \
    PATH=/usr/local/cargo/bin:${PATH}

RUN apt-get update \
    && apt-get install -y --no-install-recommends build-essential cmake \
    && rm -rf /var/lib/apt/lists/* \
    && curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs \
        | sh -s -- -y --no-modify-path --profile minimal --default-toolchain stable

WORKDIR /usr/src/app

COPY Cargo.toml Cargo.lock ./
COPY micro-gateway-rs ./micro-gateway-rs
COPY micro-sync-rs ./micro-sync-rs

RUN cargo build --locked --release -p micro-sync-rs

FROM base AS runtime

WORKDIR /app

COPY --from=builder /usr/src/app/target/release/micro-sync-rs /app/micro-sync-rs

EXPOSE 8089

CMD ["/app/micro-sync-rs"]
