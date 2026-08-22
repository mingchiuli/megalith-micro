# megalith-frontend

Vue 3 + Vite SSR frontend for Megalith. Public and administration routes render on the Bun server, prefetch their initial API data, and then hydrate into the existing interactive application in the browser. The collaborative editor, Yjs provider, AI workflow, comments, and other DOM-only integrations start after hydration.

This application is maintained inside the Megalith Micro monorepo. Run the commands below from the `megalith-frontend` directory; it remains an independently built and deployed service.

## Runtime model

- `server/production.ts` serves embedded assets and renders every application route with `Bun.serve`.
- `src/entry-server.ts` creates an isolated Vue, Pinia, Router, i18n, head, and HTTP graph for each request.
- `src/entry-client.ts` restores the serialized Pinia state and hydrates the server HTML.
- `useUniversalData` fetches route data during SSR and reuses it on the first client render.
- The Bun runtime exports correlated traces, metrics, and logs to the existing OTLP endpoint.
- Access and refresh tokens are HttpOnly cookies. Browser code does not read or persist either token.
- Login and refresh response bodies never contain token data.

## Local development

Requirements: Bun 1.4.0 and the Megalith gateway on `127.0.0.1:8088`.

```bash
bun install --frozen-lockfile
bun run dev
```

Open `http://127.0.0.1:1919`. Vite proxies browser `/api` and `/wsapi` traffic; server prefetch calls the gateway directly.

For local HTTP login, run `micro-auth` with:

```bash
MEGALITH_AUTH_COOKIE_SECURE=false
```

## Verification and production

```bash
bun run lint
bun run test:unit:run
bun run build
bun run test:ssr
bun run test:otel
bun run preview
```

Production runtime variables:

| Variable                      | Production deployment value    | Purpose                                             |
| ----------------------------- | ------------------------------ | --------------------------------------------------- |
| `PORT`                        | `1919`                         | Bun SSR listen port                                 |
| `SSR_API_BASE_URL`            | `http://micro-gateway-rs:8088` | Gateway URL on the shared Compose network           |
| `APP_ORIGIN`                  | incoming public request origin | Origin forwarded for cookie-authenticated mutations |
| `OTEL_SERVICE_VERSION`        | deployed Git commit SHA        | Immutable frontend release identifier               |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://apm-server:8200`       | Base OTLP/HTTP endpoint for all three signals       |
| `OTEL_TRACES_SAMPLER`         | `parentbased_traceidratio`     | Preserve parent sampling decisions                  |
| `OTEL_TRACES_SAMPLER_ARG`     | `0.5`                          | Sample half of new root traces                      |
| `LOG_LEVEL`                   | `info`                         | Console and OTLP application log threshold          |

Vite builds the browser and SSR modules, then `bun build --compile` embeds those modules and all client assets in `dist/bin/megalith-frontend`. The runtime container contains that executable instead of Node, Bun CLI, `node_modules`, or server source. It exposes `/actuator/health` and is published only as `mingchiuli/megalith-frontend:latest`.

The deployment host must define Compose services named `megalith-frontend`, `micro-gateway-rs`, and `apm-server` on the same Docker network. SSR traffic stays on that network through `http://micro-gateway-rs:8088`, and telemetry uses `http://apm-server:8200`; only browser traffic uses the public `/api` endpoint. The deployment workflow is the single source of these production internal URLs. Example service fragment:

```yaml
services:
  megalith-frontend:
    image: mingchiuli/megalith-frontend:latest
    restart: unless-stopped
    environment:
      PORT: 1919
      SSR_API_BASE_URL: http://micro-gateway-rs:8088
      APP_ORIGIN: https://chiu.wiki
      OTEL_SERVICE_VERSION: <deployed-git-commit-sha>
      OTEL_EXPORTER_OTLP_ENDPOINT: http://apm-server:8200
      OTEL_TRACES_SAMPLER: parentbased_traceidratio
      OTEL_TRACES_SAMPLER_ARG: '0.5'
    ports:
      - '1919:1919'
```

See [docs/ssr-architecture.md](docs/ssr-architecture.md) for the request, authentication, prefetch, and rollout design.
