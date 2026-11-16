"""Select build matrices and deployment order from a GitHub event."""

from __future__ import annotations

import json
import os
import subprocess
from collections.abc import Iterable
from collections.abc import Set as AbstractSet
from pathlib import Path
from typing import cast

from .common import entrypoint, run, write_outputs

JAVA_SERVICES = (
    "micro-user",
    "micro-auth",
    "micro-blog",
    "micro-search",
    "micro-exhibit",
)
RUST_SERVICES = ("micro-sync-rs", "micro-gateway-rs")
FRONTEND = "megalith-frontend"
SERVICE_ORDER = (*JAVA_SERVICES, *RUST_SERVICES, FRONTEND)
JAVA_DIRECTORIES = (
    "api-auth/",
    "api-user/",
    "api-blog/",
    "api-search/",
    "common-contract/",
    "common-arch/",
    "common-rpc/",
    "common-web/",
    "common-auth-web/",
    "common-observability/",
    "common-export/",
    "common-outbox/",
    "common-messaging/",
    "common-scheduling/",
    "cache/",
    "gradle/",
)
JAVA_FILES = {
    "build.gradle.kts",
    "settings.gradle.kts",
    "gradlew",
    "gradlew.bat",
    "gradle.properties",
}


def requested_services(value: str) -> set[str]:
    selected: set[str] = set()
    for service in (value or "all").split():
        if service == "all":
            selected.update(SERVICE_ORDER)
        elif service in SERVICE_ORDER:
            selected.add(service)
        else:
            raise ValueError(f"Unknown service '{service}'")
    return selected


def services_for_paths(paths: Iterable[str]) -> set[str]:
    selected: set[str] = set()
    for path in paths:
        if path.endswith(".md"):
            continue
        if path == ".github/workflows/main-services.yml" or path.startswith("scripts/"):
            selected.update(SERVICE_ORDER)
        elif path.startswith("micro-frontend/") or path in {"package.json", "bun.lock"}:
            selected.add(FRONTEND)
        elif path.startswith(JAVA_DIRECTORIES) or path in JAVA_FILES:
            selected.update(JAVA_SERVICES)
        elif path in {"Cargo.toml", "Cargo.lock", ".dockerignore"}:
            selected.update(RUST_SERVICES)
        else:
            selected.update(
                service for service in SERVICE_ORDER if path.startswith(f"{service}/")
            )
    return selected


def changed_services(before: str | None, after: str) -> set[str]:
    if before and before != "0" * 40:
        try:
            _ = run(
                "git",
                "cat-file",
                "-e",
                f"{before}^{{commit}}",
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL,
            )
        except subprocess.CalledProcessError:
            print("::warning::Previous commit is unavailable; building all services.")
            return set(SERVICE_ORDER)
        result = run(
            "git",
            "diff",
            "--no-renames",
            "--name-only",
            "-z",
            before,
            after,
            capture_output=True,
        )
    elif before == "0" * 40:
        result = run(
            "git", "ls-tree", "-r", "--name-only", "-z", after, capture_output=True
        )
    else:
        print("::warning::Push has no previous commit; building all services.")
        return set(SERVICE_ORDER)
    return services_for_paths(result.stdout.split("\0"))


def service_outputs(selected: AbstractSet[str]) -> dict[str, str]:
    java = [service for service in JAVA_SERVICES if service in selected]
    rust = [service for service in RUST_SERVICES if service in selected]
    ordered = [service for service in SERVICE_ORDER if service in selected]
    return {
        "java-services": json.dumps(java, separators=(",", ":")),
        "rust-services": json.dumps(rust, separators=(",", ":")),
        "deploy-services": " ".join(ordered),
        "has-java": str(bool(java)).lower(),
        "has-rust": str(bool(rust)).lower(),
        "has-frontend": str(FRONTEND in selected).lower(),
        "has-services": str(bool(ordered)).lower(),
    }


def pull_request_services(event: dict[str, object]) -> set[str]:
    request = cast(dict[str, object], event["pull_request"])
    base = cast(dict[str, str], request["base"])["sha"]
    head = cast(dict[str, str], request["head"])["sha"]
    try:
        for sha in (base, head):
            _ = run(
                "git", "cat-file", "-e", f"{sha}^{{commit}}",
                stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL,
            )
        ancestor = run("git", "merge-base", base, head, capture_output=True).stdout.strip()
    except subprocess.CalledProcessError:
        print("::warning::PR commits are unavailable; checking all services.")
        return set(SERVICE_ORDER)
    return changed_services(ancestor, head)


def main() -> None:
    event = cast(
        dict[str, object],
        json.loads(Path(os.environ["GITHUB_EVENT_PATH"]).read_text(encoding="utf-8")),
    )
    if os.environ["GITHUB_EVENT_NAME"] == "workflow_dispatch":
        selected = requested_services(os.environ.get("SERVICES_INPUT", "all"))
    elif os.environ["GITHUB_EVENT_NAME"] == "pull_request":
        selected = pull_request_services(event)
    else:
        before = event.get("before")
        if before is not None and not isinstance(before, str):
            raise ValueError("Push event contains an invalid previous commit")
        selected = changed_services(before, os.environ["GITHUB_SHA"])
    outputs = service_outputs(selected)
    write_outputs(outputs)
    print(f"Selected services: {outputs['deploy-services'] or 'none'}")


if __name__ == "__main__":
    entrypoint(main)
