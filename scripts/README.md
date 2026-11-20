# GitHub Actions scripts

The workflows keep job dependencies, matrices, actions, and secrets in YAML. All
`run` steps call these Python entry points, which use only the standard library:

Run them as modules from the repository root, for example:

```bash
python3 -m scripts.checks java
```

Both workflows use the repository root as the working directory for `run` steps.
Relative imports need no editor-specific configuration.

| Script | Responsibility |
| --- | --- |
| `checks.py` | Java, Rust, frontend quality checks, and Java image builds |
| `services.py` | Changed-service selection and GitHub job outputs |
| `cache_release.py` | Cache/native-hints release tags, Maven Central version checks, joint publication, and finalization |
| `wireguard.py` | Temporary runner VPN setup and cleanup |
| `deploy.py` | Compose deployment, health checks, image cleanup, and nginx reload |
| `sync_search.py` | Inspect the live BLOG outbox and search queues, or rebuild all three Meilisearch projections during a maintenance window |

Changes under `scripts/` trigger the service workflow and select all services, except
Markdown-only changes. Missing previous commits after rewritten history also
select all services.

Python 3.9+ is required on the Actions runner and the SSH deployment host. The
deployment preparation step passes the standalone Python source through the
existing SSH action, so the host needs no checkout or third-party Python packages.
The application container images do not need Python. The SSH user's working directory must contain
the deployment Compose files, and the user must have permission to run Docker and Compose.

Workflow inputs and credentials are passed through environment variables. Build
tools run as argument lists, and any failed command stops its step. Deployments
remain sequential, wait for container health, and remove temporary Compose
overrides even when a deployment fails.

For the first Meilisearch deployment, a host can stage `megalith-ops/sync_search.py` and
`megalith-ops/search-cutover.pending` beside its Compose files. When deploying `micro-search`,
the deployment step pulls the release image, runs the staged synchronizer in maintenance mode
and clears the marker after the index swap and service checks succeed. The synchronizer starts OpenObserve and the new search consumers,
which process any queued blog changes before the rebuild. Select `all` in the first workflow dispatch so every exporter picks up the
OpenObserve settings.

Pull requests use their base/head merge base for change selection, falling back to all services when
commits are unavailable. PRs run quality checks only; image publication and deployment jobs reject
PR events. Java quality checks include auth, user, blog, exhibit, search, and cache integration tests.
The exhibit image build also verifies native startup and Redis queries over Docker DNS with an
isolated Redis/RabbitMQ network. The fixture declares the blog-owned exchange and exhibit queue
before startup, matching production deployment order.
Frontend quality runs the full `frontend:check`, including SSR builds and smoke tests. Workflow
selection/guard tests run through `python3 -m unittest discover -s scripts/tests`.
