# Use the official Rust image as the base image
FROM rust:latest AS builder

# Set the working directory
WORKDIR /usr/src/app

# Copy the entire project
COPY . .

# Build the application
RUN cargo build --release

# Use a minimal base image for the final stage
FROM debian:buster-slim

# Install necessary runtime dependencies
RUN apt-get update && apt-get install -y \
    ca-certificates \
    tzdata \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create app directory
WORKDIR /app

# Copy the build artifact from the builder stage
COPY --from=builder /usr/src/app/target/release/micro-gateway-rs /app/

# Make the binary executable
RUN chmod +x /app/micro-gateway-rs

# Set the startup command
CMD ["/app/micro-gateway-rs"]