# Use the official Rust image as the base image
FROM rust:latest AS builder

# Set the working directory
WORKDIR /usr/src/app

# Copy workspace manifest
COPY Cargo.toml Cargo.lock ./

# Copy all workspace members
COPY micro-gateway-rs ./micro-gateway-rs
COPY micro-sync-rs ./micro-sync-rs

# Install musl target
RUN rustup target add x86_64-unknown-linux-musl

# Build only micro-sync-rs
RUN cargo build --release --target x86_64-unknown-linux-musl -p micro-sync-rs

# Use Alpine for minimal image size (matches musl)
FROM alpine:latest

# Install runtime dependencies
RUN apk add --no-cache ca-certificates tzdata

# Create app directory
WORKDIR /app

# Copy the build artifact from the builder stage
COPY --from=builder /usr/src/app/target/x86_64-unknown-linux-musl/release/micro-sync-rs /app/

# Expose the port (adjust based on your application)
EXPOSE 8089

# Set the startup command
CMD ["/app/micro-sync-rs"]