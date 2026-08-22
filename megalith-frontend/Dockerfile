FROM oven/bun:1.4.0 AS build

WORKDIR /app
COPY package.json bun.lock ./
COPY megalith-frontend/package.json megalith-frontend/package.json
RUN bun install --frozen-lockfile --filter megalith-frontend

COPY megalith-frontend megalith-frontend
WORKDIR /app/megalith-frontend
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
  /app/megalith-frontend/dist/bin/megalith-frontend /app/megalith-frontend

USER megalith
EXPOSE 1919
STOPSIGNAL SIGTERM

CMD ["/app/megalith-frontend"]
