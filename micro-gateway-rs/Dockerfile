# Use the official Rust image as the base image
FROM rust:latest AS builder

# Set the working directory
WORKDIR /usr/src/app

# Copy the Cargo.toml and Cargo.lock files
COPY Cargo.toml Cargo.lock ./

# Build dependencies only
RUN cargo build --release

# Remove the dummy source code
RUN rm -rf src

# Copy the source code
COPY . .

# Build the application
RUN cargo build --release

# Use a minimal base image for the final stage
FROM debian:buster-slim

# Install necessary runtime dependencies
RUN apt-get update && apt-get install -y \
    ca-certificates \
    tzdata \
    && rm -rf /var/lib/apt/lists/*

# Copy the build artifact from the builder stage
COPY --from=builder /usr/src/app/target/release/micro-gateway-rs /app/

# Set the startup command
CMD ["./micro-gateway-rs"]