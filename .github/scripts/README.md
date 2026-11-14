# GitHub Actions scripts

The workflows keep job dependencies, matrices, actions, and secrets in YAML. All
`run` steps call these Python entry points, which use only the standard library:

Run them as modules from `.github`, for example:

```bash
cd .github
python3 -m scripts.checks java
```

Both workflows set this working directory for `run` steps. Build commands still
run from the repository root; relative imports need no editor-specific configuration.

| Script | Responsibility |
| --- | --- |
| `checks.py` | Java, Rust, frontend quality checks, and Java image builds |
| `services.py` | Changed-service selection and GitHub job outputs |
| `cache_release.py` | Cache release tags, Maven Central version checks, publication, and finalization |
| `wireguard.py` | Temporary runner VPN setup and cleanup |
| `deploy.py` | Compose deployment, health checks, image cleanup, and nginx reload |

Changes under `.github/scripts/` trigger the service workflow and select all services, except
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
