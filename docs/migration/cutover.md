# Base, auth and exhibit cutover

Base serves both user and blog on port 8082. The Rust auth and exhibit containers retain service names
`micro-auth` and `micro-exhibit` and ports 8081 and 8083. Java cache releases remain independent.
The original implementation and file inventory are identified by Git revision `c1d9bb93`.

## Prepare and verify

- Build the base Native Image and the two Rust release images. Retain the exact image digests and
  Compose configuration for both releases. Run both versions against separate copies of the same
  test fixtures. Each stack needs a separate Redis, RabbitMQ vhost, and Meilisearch indexes.
- Use the base Native Image when exercising menus containing a parent and child. The build-time
  Hibernate enhancer supplies lazy associations used by the two database foreign keys.
- Inspect the recommendation routing rows in the target database. The navigation snapshot used for
  the browser run contained the older paged route. Confirm that an anonymous GET to the current
  route reaches exhibit through the gateway before reopening ingress.

  ```sql
  SELECT id, route_pattern, service_host, service_port
  FROM m_authority
  WHERE route_pattern IN ('/public/blog/recommendations', '/public/blog/recommendations/page/*');
  ```
- In the deployment host's Compose definition, replace user and blog with `micro-base`. Combine
  datasource, Redis, broker, outbox, OSS, registration URL, collaboration and maintenance settings.
  Keep production credentials in the deployment environment. Configure `MEGALITH_BLOG_BLOG_URL`
  and `MEGALITH_BLOG_USER_URL` as `http://micro-base:8082/inner`. Point search's blog source to
  that address too. Supply the same auth JWT secret, issuer, audience, lifetimes and cookie policy
  to the Rust auth service. It accepts dotted Spring property names and uppercase environment keys.
- Verify registration links, authorization targets, health probes, HTTPS origin allowlists, SSR
  prefetch, WebSocket forwarding, retry queues, external test integrations, and both producer
  endpoints in the isolated stack. The outbox management paths use
  `/actuator/outbox/{producer}` and `/actuator/outbox/{producer}/{eventId}`.
- Run the browser suite with `E2E_BASE_URL`, `E2E_PEER_BASE_URL` (the other release), `E2E_USERNAME`, and `E2E_PASSWORD` set for the
  isolated deployment. The SQL navigation fixture supplies the normal Chinese operation labels.
  The suite expects a dedicated administrator with nickname `E2E Admin`, authorized menu entries,
  and a regular role with code `user`. It creates and deletes fixture rows. Run
  `bun run --filter megalith-frontend test:e2e`. Use `E2E_SELF_SIGNED=1` only for the fixture
  TLS certificate. Playwright reports and traces are local build artifacts and can contain session
  credentials. Share redacted evidence only.

## Maintenance window

1. Close ingress and stop the affected services, consumers and scheduled jobs. Save the database,
   route IDs and previous host/port pairs, relevant Redis state, broker state and exact image
   digests. Keep the `USER` and `BLOG` production identities and existing durable queues.
2. Execute [forward.sql](../sql/base/forward.sql) in the application database. Set the old
   and new host/port session variables where the installation uses different addresses. The
   preflight returns the IDs of existing orphans and aborts before modifying rows if any exist.
   The script converts root menu parent IDs to SQL NULL, adds both foreign keys, and records
   the previous targets in `m_base_cutover_route`.
3. While auth replicas are stopped, delete the exact v1 route-cache key. Its suffix is the
   SHA-256 of the two raw bytes `[]`:
   `auth-system-authorities:v1:4f53cda18c2baa0c0354bb5f9a3ecbe5ed12ab4d8e11ba873c2f11161202b945`.
   This preserves session cookies, read tokens, counters, unrelated caches and collaboration rooms.
4. Start base, auth and exhibit, then the direct consumers and gateway with their release
   configuration. Check health and both outbox backlogs. Before opening ingress, use a dedicated
   browser session to verify an existing login, refresh, admin menu and protected article.
   Use independent browser sessions for public reads and collaboration.
5. If a check fails before reopening ingress, stop the new workers, execute
   [rollback.sql](../sql/base/rollback.sql), restore the previous Compose file and digests,
   invalidate the same route key, and rerun the browser checks. Retain the route snapshot and
   backups until release acceptance.

## Live integration fixtures

Run `bun run --filter megalith-frontend test:e2e:live` against a dedicated full-stack
installation. Supply `E2E_EMAIL_RECIPIENT` and `E2E_SMS_RECIPIENT` as the login names of
fixture users. `E2E_RECEIVE_CODE_PROGRAM` is an executable that receives the channel, recipient,
and issuance timestamp as separate arguments, reads the actual mailbox or SMS inbox, and writes
the received code to stdout. Each browser receives its own code.

Set `E2E_OSS_ORIGIN` to the test bucket's serving origin. Select an installed text model
with `E2E_OLLAMA_MODEL` and configure the frontend's `VITE_AI_IMAGE_MODEL` against a server
that can run it. Use dedicated `VITE_GISCUS_REPO`, `VITE_GISCUS_REPO_ID`, `VITE_GISCUS_CATEGORY`,
`VITE_GISCUS_CATEGORY_ID`, and `VITE_DISQUS_SHORTNAME` at build time. Use
`E2E_GISCUS_REPO`, `E2E_DISQUS_SHORTNAME` and `E2E_COMMENT_ARTICLE_PATH` to verify the
rendered widgets. Authenticated comment posting and cleanup require the designated test accounts.

The Java/Rust L2 interoperation test starts Redis in Testcontainers, loads through the Java
Spring proxy, verifies the value and tracking lease in Rust, evicts it via the reliable topic,
and reads the Rust refill through Java. Run it from the workspace with
`CACHE_RUST_INTEROP=1 ./gradlew :cache:integrationTest --tests '*CacheRustInteropIntegrationTest'`.

## Repeatable database and browser evidence

With a disposable MariaDB fixture container running, prepare a fresh, named database for each
release. The preparation tool copies table definitions and the three navigation and routing tables.
Users, credentials, content and outbox rows are generated or exercised in the fixture. Set
`E2E_PASSWORD` in the environment before running the command. The resulting admin login is
`e2e-admin`.

```sh
python3 -m scripts.browser_fixture --source-container megalith-mariadb-1 \
  --target-container e2e-mariadb --fixture-db candidate --candidate --port-offset 20000
```

The port offset applies to the original local 8081–8089 service ports. Set each fixture service's
server port and downstream URLs accordingly. Create the legacy database separately using the same
source and a different port offset. The candidate fixture rehearses forward, rollback and forward
against the database. Test origins should terminate at HTTPS nginx with `/api` and `/wsapi`
proxied to the matching gateway. Set `APP_ORIGIN` to the external HTTPS origin and
`SSR_API_BASE_URL` to the fixture gateway. Build the frontend with `VITE_BASE_WS_URL=/wsapi`.

The browser suite emits a JSON report at `build/playwright-results.json` or the path in
`E2E_REPORT`. Export pass/fail records without cookies, inbox codes or invitation URLs with
`python3 -m scripts.browser_evidence REPORT.json --output evidence.csv`.

To check that all configured browser projects ran the exact same test inventory as the candidate,
generate an unfiltered manifest and supply it when exporting the reports. Run this independently for
the application suite and the live provider suite. A missing case, duplicate case, failed case,
skipped case, or retry produces a failing command.

```sh
cd micro-frontend
PLAYWRIGHT_JSON_OUTPUT_FILE=build/manifest.json bun run test:e2e --list --reporter=json
E2E_REPORT=build/actual.json bun run test:e2e
cd ..
python3 -m scripts.browser_evidence micro-frontend/build/actual.json \
  --manifest micro-frontend/build/manifest.json --output browser-evidence.csv
```
