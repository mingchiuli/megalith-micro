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

Changes under `scripts/` trigger the service workflow and select all services, except
Markdown-only changes. Missing previous commits after rewritten history also
select all services.

Python 3.9+ is required on the Actions runner and the SSH deployment host. The
deployment preparation step passes the standalone Python source through the
existing SSH action, so the host needs no checkout or third-party Python packages.
The application container images do not need Python. The SSH user's working
directory, Docker permissions, and Compose files remain the same as before.

Workflow inputs and credentials are passed through environment variables. Build
tools run as argument lists, and any failed command stops its step. Deployments
remain sequential, wait for container health, and remove temporary Compose
overrides even when a deployment fails.

Pull requests use their base/head merge base for change selection, falling back to all services when
commits are unavailable. PRs run quality checks only; image publication and deployment jobs reject
PR events. Java quality checks include auth, user, blog, exhibit, search, and cache integration tests.
The exhibit image build also verifies native startup and Redis queries over Docker DNS with an
isolated Redis/RabbitMQ network. The fixture declares the blog-owned exchange and exhibit queue
before startup, matching production deployment order.
Frontend quality runs the full `frontend:check`, including SSR builds and smoke tests. Workflow
selection/guard tests run through `python3 -m unittest discover -s scripts/tests`.
