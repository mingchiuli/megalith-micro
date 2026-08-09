# megalith-frontend

Vue 3 + Vite SSR frontend for Megalith. Public and administration routes render on the Node server, prefetch their initial API data, and then hydrate into the existing interactive application in the browser. The collaborative editor, Yjs provider, AI workflow, comments, and other DOM-only integrations start after hydration.

## Runtime model

- `server/index.mjs` serves the built assets and renders every application route.
- `src/entry-server.ts` creates an isolated Vue, Pinia, Router, i18n, head, and HTTP graph for each request.
- `src/entry-client.ts` restores the serialized Pinia state and hydrates the server HTML.
- `useUniversalData` fetches route data during SSR and reuses it on the first client render.
- Access and refresh tokens are HttpOnly cookies. Browser code does not read or persist either token.
- Login and refresh response bodies never contain token data.

## Local development

Requirements: Node.js 24 and the Megalith gateway on `127.0.0.1:8088`.

```bash
npm ci
npm run dev
```

Open `http://127.0.0.1:1919`. Vite proxies browser `/api` and `/wsapi` traffic; server prefetch calls the gateway directly.

For local HTTP login, run `micro-auth` with:

```bash
MEGALITH_AUTH_COOKIE_SECURE=false
```

## Verification and production

```bash
npm run lint
npm run test:unit -- --run
npm run build
npm run test:ssr
npm run preview
```

Production runtime variables:

| Variable           | Production deployment value    | Purpose                                             |
| ------------------ | ------------------------------ | --------------------------------------------------- |
| `PORT`             | `1919`                         | Node SSR listen port                                |
| `SSR_API_BASE_URL` | `http://micro-gateway-rs:8088` | Gateway URL on the shared Compose network           |
| `APP_ORIGIN`       | incoming public request origin | Origin forwarded for cookie-authenticated mutations |

The production container exposes `/actuator/health` and is published only as `mingchiuli/megalith-frontend:latest`.

The deployment host must define Compose services named `megalith-frontend` and `micro-gateway-rs` on the same Docker network. SSR traffic stays on that network through `http://micro-gateway-rs:8088`; only browser traffic uses the public `/api` endpoint. The deployment workflow is the single source of the production internal URL. Example service fragment:

```yaml
services:
  megalith-frontend:
    image: mingchiuli/megalith-frontend:latest
    restart: unless-stopped
    environment:
      PORT: 1919
      SSR_API_BASE_URL: http://micro-gateway-rs:8088
      APP_ORIGIN: https://chiu.wiki
    ports:
      - '1919:1919'
```

See [docs/ssr-architecture.md](docs/ssr-architecture.md) for the request, authentication, prefetch, and rollout design.
