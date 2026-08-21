FROM oven/bun:1.4.0 AS build

WORKDIR /app
COPY package.json bun.lock ./
RUN bun install --frozen-lockfile

COPY . .
RUN bun run build

FROM debian:bookworm-slim AS runtime

ARG DEBIAN_FRONTEND=noninteractive
ENV NODE_ENV=production \
    PORT=1919

RUN apt-get update \
    && apt-get install --yes --no-install-recommends ca-certificates curl tzdata \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system megalith \
    && useradd --system --gid megalith --home-dir /app --no-create-home megalith

WORKDIR /app
COPY --from=build --chown=megalith:megalith --chmod=0755 \
  /app/dist/bin/megalith-frontend /app/megalith-frontend

USER megalith
EXPOSE 1919
STOPSIGNAL SIGTERM

HEALTHCHECK --interval=10s --timeout=3s --start-period=10s --retries=6 \
  CMD curl --fail --silent --show-error http://127.0.0.1:1919/actuator/health >/dev/null || exit 1

CMD ["/app/megalith-frontend"]
