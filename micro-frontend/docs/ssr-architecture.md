# SSR Architecture

## Request flow

1. The Bun server receives the browser request and creates a request-scoped application.
2. The incoming `Cookie`, `Accept-Language`, and origin are passed to that application only.
3. Vue Router resolves the route. Protected routes load the menu and current user before rendering.
4. Route components run `onServerPrefetch`, call `micro-gateway-rs` over the shared Docker network, and place results in `ssrDataStore`.
5. Vue renders HTML; Unhead renders metadata; Pinia state is serialized with `devalue`.
6. The browser restores that state and hydrates the existing HTML without repeating initial API calls.

Request-scoped Router, Pinia, i18n, HTTP interceptors, cookies, and refresh promises prevent data from leaking between concurrent users.

## Data boundaries

Public blog lists, blog detail, statistics, registration checks, and initial administration tables are prefetched. Subsequent searches, pagination, dialogs, writes, downloads, and uploads remain client interactions.

The editor shell can be rendered by the server, but CodeMirror, Yjs, WebSocket tickets, AI model discovery, and third-party comments are client-only. These integrations depend on browser APIs and do not contribute useful indexable HTML.

## Authentication

`micro-auth` issues `megalith_access_token` and `megalith_refresh_token` as HttpOnly cookies. For browser HTTP requests, the gateway derives the internal Bearer credential exclusively from the access cookie. The frontend forwards cookies during SSR, captures refreshed `Set-Cookie` headers, and sends them back with the HTML response.

Login and refresh responses contain no token data. Access and refresh tokens are transported only through HttpOnly cookies and are never persisted in `localStorage`.

Cookie-authenticated requests are checked against the configured frontend origin at the gateway. Production cookies default to `Secure` and `SameSite=Strict`; local HTTP development must set `MEGALITH_AUTH_COOKIE_SECURE=false` for `micro-auth`.

## Observability

The Bun process initializes OpenTelemetry before `Bun.serve`. A native request wrapper extracts W3C context and creates HTTP server spans, `ssr.render` records the Vue render, and server-side Axios calls create HTTP client spans that propagate the same context to `micro-gateway-rs`. Hydrated browser API and WebSocket traffic bypasses the SSR server and starts at the gateway; the browser does not send telemetry directly to the private APM endpoint.

The service exports OTLP/HTTP Protobuf traces, metrics, and logs. Metrics include HTTP client/server durations, Bun CPU/RSS/uptime, JavaScriptCore heap measurements, event-loop delay, plus `ssr.render.duration`, `ssr.render.requests`, `ssr.render.errors`, and `ssr.render.active`. Bun does not expose the V8 GC and Node event-loop-utilization semantics, so those metrics are intentionally absent. Application logs are written as structured JSON to stdout and exported with active trace and span identifiers. Cookies, tokens, request bodies, and serialized Pinia state are not recorded. Dynamic SSR paths are normalized to Vue route patterns, and inbound query values are redacted.

`OTEL_EXPORTER_OTLP_ENDPOINT` configures the base endpoint for all signals, while `OTEL_SERVICE_VERSION` identifies the deployed Git commit. New root traces default to the parent-based trace ID ratio sampler at `0.05`; standard signal-specific `OTEL_*` variables can override the endpoint, exporter, sampler, batching, and export intervals. `/actuator/health` is excluded from tracing.

On `SIGTERM` or `SIGINT`, readiness changes to 503, `Bun.serve` stops accepting requests, active connections receive up to ten seconds to finish, and all three OTel providers shut down and flush before the process exits.

## Status, caching, and failure behavior

- Unknown routes return a rendered 404 with HTTP status 404.
- Authentication redirects are HTTP 302 responses during SSR.
- HTML responses use `Cache-Control: private, no-store` because they may contain user-specific state.
- Fingerprinted assets are immutable and cached for one year.
- Initial prefetch failures flow through the route or server error path; interactive request failures use the existing Element Plus notification behavior.

## Deployment

The multi-stage Docker build runs Vite under Bun and compiles `dist/bin/megalith-frontend`, a standalone executable containing the Bun runtime, server code, SSR bundle, and client assets. The Debian runtime image copies only that executable and contains no Node, Bun CLI, `node_modules`, or server source. Deployment injects `SSR_API_BASE_URL=http://micro-gateway-rs:8088`, `OTEL_SERVICE_VERSION=<git-sha>`, and `OTEL_EXPORTER_OTLP_ENDPOINT=http://apm-server:8200`; the frontend, gateway, and APM Server Compose services must share a Docker network. CI verifies lint, unit tests, type checking, both Vite builds, standalone SSR behavior, all three OTLP signals, W3C propagation, Bun runtime metrics, and graceful shutdown before publishing `mingchiuli/megalith-frontend:latest`.

Deployment records the previous local image ID, pulls `latest`, recreates the Compose service, and waits for its container health check. If the health check fails or times out, the workflow prints the container logs and exits with a failure; it does not restore the previous image or container. After a successful health check, the previous local image is removed when its ID differs from the current image. No SHA image tag is pushed.
