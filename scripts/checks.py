"""Named quality and image-build commands used by the service workflow."""

import os

from .common import entrypoint, parse_command, run
from .services import JAVA_SERVICES

COMMANDS = {
    "java": (("./gradlew", "check"),),
    "java-integration": (
        (
            "./gradlew",
            ":micro-base:integrationTest",
            ":cache:integrationTest",
        ),
    ),
    "base-native-smoke": (
        (
            "./gradlew",
            ":micro-base:integrationTest",
            "--tests",
            "*BlogApplicationStartupIntegrationTest",
        ),
    ),
    "scripts": (("python3", "-m", "unittest", "discover", "-s", "scripts/tests"),),
    "rust-fmt": (("cargo", "fmt", "--all", "--", "--check"),),
    "rust-clippy": (
        ("cargo", "clippy", "--workspace", "--all-targets", "--", "-D", "warnings"),
    ),
    "rust-test": (("cargo", "test", "--workspace"),),
    "search-integration": (("cargo", "test", "-p", "micro-search-rs", "--test", "service", "--", "--ignored"),),
    "rust-redis": (
        (
            "cargo",
            "test",
            "-p",
            "micro-sync-rs",
            "redis_store_round_trip_when_configured",
            "--",
            "--ignored",
        ),
    ),
    "frontend-install": (("bun", "install", "--frozen-lockfile"),),
    "frontend-lint": (("bun", "run", "--filter", "megalith-frontend", "lint"),),
    "frontend-quality": (
        ("bun", "run", "frontend:check"),
    ),
    "frontend-build": (
        ("bun", "run", "frontend:build"),
        ("bun", "run", "frontend:test:ssr"),
        ("bun", "run", "frontend:test:otel"),
    ),
    "java-info": (("java", "--version"), ("native-image", "--version")),
}


def main() -> None:
    command = parse_command(__doc__, [*COMMANDS, "java-aot", "java-image"])
    if command == "base-native-smoke" and not os.environ.get("MICRO_BASE_NATIVE_EXECUTABLE"):
        raise ValueError("MICRO_BASE_NATIVE_EXECUTABLE is required for the native image smoke test")
    if command in {"java-aot", "java-image"}:
        service = os.environ["SERVICE"]
        if service not in JAVA_SERVICES:
            raise ValueError(f"Unknown Java service '{service}'")
        task = "processAot" if command == "java-aot" else "bootBuildImage"
        _ = run("./gradlew", f":{service}:{task}")
        return
    if command == "java-info":
        for variable in ("GRAALVM_HOME", "JAVA_HOME"):
            print(f"{variable}: {os.environ.get(variable, '')}", flush=True)
    for args in COMMANDS[command]:
        _ = run(*args)


if __name__ == "__main__":
    entrypoint(main)
