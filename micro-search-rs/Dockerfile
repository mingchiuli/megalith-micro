ARG BUILDER_IMAGE=rust:1.98-bookworm
FROM ${BUILDER_IMAGE} AS builder
WORKDIR /usr/src/app
COPY Cargo.toml Cargo.lock ./
COPY crates ./crates
COPY micro-exhibit-rs ./micro-exhibit-rs
COPY micro-auth-rs ./micro-auth-rs
COPY micro-search-rs ./micro-search-rs
COPY micro-gateway-rs ./micro-gateway-rs
COPY micro-sync-rs ./micro-sync-rs
RUN cargo build --locked --release -p micro-search-rs

FROM debian:bookworm-slim
WORKDIR /app
COPY --from=builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/ca-certificates.crt
COPY --from=builder /usr/share/zoneinfo /usr/share/zoneinfo
COPY --from=builder /usr/src/app/target/release/micro-search-rs /app/micro-search
# The existing Compose probe uses the previous Buildpacks health-check path.
RUN mkdir -p /layers/paketo-buildpacks_health-checker/thc/bin \
    && ln -s /app/micro-search /layers/paketo-buildpacks_health-checker/thc/bin/thc
HEALTHCHECK CMD ["/app/micro-search", "health"]
EXPOSE 8085
CMD ["/app/micro-search"]
