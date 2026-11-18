"""Prepare an SSH command on the runner, or deploy services on the Docker host.

The prepared command carries this standalone script to the existing SSH action;
the host needs Python 3.9+ but does not need a checkout or Python packages.
"""

import argparse
import json
import os
import re
import shlex
import subprocess
import sys
import tempfile
import time
from collections.abc import Sequence
from pathlib import Path
from typing import TypedDict

SERVICE_ORDER = (
    "micro-user",
    "micro-auth",
    "micro-blog",
    "micro-exhibit",
    "micro-search",
    "micro-sync-rs",
    "micro-gateway-rs",
    "megalith-frontend",
)
HEALTH_FORMAT = (
    "{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}"
)


def run(
    *args: str, capture: bool = False, check: bool = True
) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        args, check=check, text=True, stdout=subprocess.PIPE if capture else None
    )


def compose_file(service: str) -> str:
    if service == "micro-gateway-rs":
        return "docker-compose.infrastructure.yml"
    for candidate in (
        "compose.yaml",
        "compose.yml",
        "docker-compose.yaml",
        "docker-compose.yml",
    ):
        if Path(candidate).is_file():
            return candidate
    raise ValueError("No Compose file found")


class ServiceConfig(TypedDict, total=False):
    image: str
    environment: dict[str, str]


class ComposeOverride(TypedDict):
    services: dict[str, ServiceConfig]


def compose_override(service: str, version: str) -> tuple[str, ComposeOverride]:
    image = (
        "mingchiuli/megalith-frontend:latest"
        if service == "megalith-frontend"
        else (f"mingchiuli/megalith-{service}:latest")
    )
    config: ServiceConfig = {"image": image}
    if service == "megalith-frontend":
        config["environment"] = {
            "SSR_API_BASE_URL": "http://micro-gateway-rs:8088",
            "OTEL_SERVICE_VERSION": version,
            "OTEL_EXPORTER_OTLP_ENDPOINT": "http://apm-server:8200",
        }
    elif service == "micro-blog":
        config["environment"] = {"MEGALITH_BLOG_SYNC_URL": "http://micro-sync-rs:8089"}
    # JSON is also YAML and safely preserves values without a YAML dependency.
    return image, {"services": {service: config}}


def wait_for_health(compose: Sequence[str], service: str, container: str) -> None:
    for attempt in range(60):
        status = run(
            "docker", "inspect", "-f", HEALTH_FORMAT, container, capture=True
        ).stdout.strip()
        if status == "healthy":
            return
        if status == "unhealthy" or attempt == 59:
            _ = run(*compose, "ps", service, check=False)
            _ = run("docker", "logs", "--tail=200", container, check=False)
            raise RuntimeError(f"Service {service} failed its health check: {status}")
        time.sleep(2)


def deploy_service(service: str, version: str) -> None:
    image, override = compose_override(service, version)
    base_file = compose_file(service)
    with tempfile.TemporaryDirectory(prefix="megalith-deploy-") as directory:
        override_file = Path(directory) / "compose.json"
        _ = override_file.write_text(json.dumps(override), encoding="utf-8")
        compose = ("docker", "compose", "-f", base_file, "-f", str(override_file))
        print(f"Deploying {service}", flush=True)
        previous = run(
            "docker",
            "image",
            "inspect",
            image,
            "--format",
            "{{.Id}}",
            capture=True,
            check=False,
        )
        previous_id = previous.stdout.strip() if previous.returncode == 0 else ""
        _ = run("docker", "pull", image)
        _ = run(*compose, "rm", "-sf", service)
        _ = run(*compose, "up", "-d", "--no-deps", "--force-recreate", service)
        container = run(*compose, "ps", "-q", service, capture=True).stdout.strip()
        if not container:
            _ = run(*compose, "ps", service, check=False)
            raise RuntimeError(f"No container found for {service}")
        wait_for_health(compose, service, container)
        current_id = run(
            "docker", "image", "inspect", image, "--format", "{{.Id}}", capture=True
        ).stdout.strip()
        if previous_id and previous_id != current_id:
            _ = run("docker", "image", "rm", previous_id, check=False)
    if service == "micro-gateway-rs":
        containers = run(
            "docker",
            "ps",
            "--format",
            "{{.ID}}\t{{.Image}}\t{{.Command}}\t{{.Names}}",
            capture=True,
        )
        for line in containers.stdout.splitlines():
            container, description = line.split("\t", 1)
            if re.search(r"\bnginx\b", description):
                _ = run("docker", "exec", container, "nginx", "-s", "reload")


def validate_services(services: str) -> list[str]:
    selected = services.split()
    if not selected:
        raise ValueError("No deployment services selected")
    for service in selected:
        if service not in SERVICE_ORDER:
            raise ValueError(f"Unknown service '{service}'")
    return selected


def prepare_command(services: str, version: str) -> str:
    _ = validate_services(services)
    source = Path(__file__).read_text(encoding="utf-8")
    return shlex.join(
        (
            "python3",
            "-c",
            source,
            "remote",
            "--services",
            services,
            "--version",
            version,
        )
    )


class Arguments(argparse.Namespace):
    command: str = ""
    services: str = ""
    version: str = ""


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    _ = commands.add_parser("prepare")
    remote = commands.add_parser("remote")
    _ = remote.add_argument("--services", required=True)
    _ = remote.add_argument("--version", required=True)
    args = parser.parse_args(namespace=Arguments())
    if args.command == "prepare":
        from .common import write_outputs

        write_outputs(
            {
                "script": prepare_command(
                    os.environ["DEPLOY_SERVICES"], os.environ["SERVICE_VERSION"]
                )
            }
        )
    else:
        for service in validate_services(args.services):
            deploy_service(service, args.version)


if __name__ == "__main__":
    try:
        main()
    except subprocess.CalledProcessError as error:
        sys.exit(error.returncode if error.returncode > 0 else 1)
    except (OSError, ValueError, RuntimeError) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
