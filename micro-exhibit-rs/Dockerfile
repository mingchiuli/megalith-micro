ARG BUILDER_IMAGE=rust:1.98-bookworm
FROM ${BUILDER_IMAGE} AS builder
WORKDIR /usr/src/app
ARG CARGO_BUILD_JOBS
ARG TARGETARCH
COPY Cargo.toml Cargo.lock ./
COPY crates ./crates
COPY micro-exhibit-rs ./micro-exhibit-rs
COPY micro-auth-rs ./micro-auth-rs
COPY micro-search-rs ./micro-search-rs
COPY micro-gateway-rs ./micro-gateway-rs
COPY micro-sync-rs ./micro-sync-rs
RUN --mount=type=cache,target=/usr/local/cargo/registry,sharing=locked \
    --mount=type=cache,id=megalith-cargo-target-${TARGETARCH},target=/usr/src/app/target,sharing=locked \
    cargo build --locked --release -p micro-exhibit-rs && cp target/release/micro-exhibit-rs /tmp/micro-exhibit-rs

FROM debian:bookworm-slim
WORKDIR /app
COPY --from=builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/ca-certificates.crt
COPY --from=builder /usr/share/zoneinfo /usr/share/zoneinfo
COPY --from=builder /tmp/micro-exhibit-rs /app/micro-exhibit
# The existing Compose probe uses the previous Buildpacks health-check path.
RUN mkdir -p /layers/paketo-buildpacks_health-checker/thc/bin \
    && ln -s /app/micro-exhibit /layers/paketo-buildpacks_health-checker/thc/bin/thc
HEALTHCHECK CMD ["/app/micro-exhibit", "health"]
EXPOSE 8083
CMD ["/app/micro-exhibit"]
