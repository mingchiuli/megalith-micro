"""Rebuild the three search indexes on a single Linux Docker Compose host."""

from __future__ import annotations

import argparse
import fcntl
import json
import subprocess
import sys
import tempfile
import time
from collections.abc import Sequence
from http.client import HTTPResponse
from pathlib import Path
from typing import TypedDict, cast
from urllib.error import URLError
from urllib.request import ProxyHandler, Request, build_opener

HTTP = build_opener(ProxyHandler({}))
QUEUES = {
    base + suffix
    for base in ("blog.change.queue.es", "blog.admin.change.queue.es")
    for suffix in ("", ".retry.1", ".retry.2", ".retry.3", ".dlq")
}


class Arguments(argparse.Namespace):
    compose: Path = Path("docker-compose.yml")
    infra: Path = Path("docker-compose.infrastructure.yml")
    apply: bool = False
    first_release: bool = False
    drain_timeout: int = 300
    rebuild_timeout: int = 3600


class SourceStatus(TypedDict):
    readOnly: bool
    readyEvents: int
    pausedEvents: int
    total: int
    publishedTotal: int
    revisionTotal: int


class RebuiltIndex(TypedDict):
    index: str
    documents: int


def output(*args: str) -> str:
    return subprocess.check_output(args, text=True).strip()


def container(compose: Sequence[str], service: str) -> str:
    ids = output(*compose, "ps", "-q", service).split()
    if len(ids) != 1:
        raise RuntimeError(f"Expected one running {service} container, found {len(ids)}")
    return ids[0]


def address(compose: Sequence[str], service: str, port: int) -> str:
    raw = output("docker", "inspect", "--format", "{{json .NetworkSettings.Networks}}",
                 container(compose, service))
    networks = cast(dict[str, dict[str, str]], json.loads(raw))
    ips = [net["IPAddress"] for net in networks.values() if net.get("IPAddress")]
    if len(ips) != 1:
        raise RuntimeError(f"Cannot determine a unique bridge address for {service}")
    return f"http://{ips[0]}:{port}"


def request(url: str, method: str = "GET", timeout: int = 15) -> dict[str, object]:
    with cast(HTTPResponse, HTTP.open(Request(url, method=method), timeout=timeout)) as response:
        return cast(dict[str, object], json.load(response))


def data(url: str, method: str = "GET", timeout: int = 15) -> object:
    result = request(url, method, timeout)
    if result.get("code") != 200:
        raise RuntimeError(f"Application rejected {method} {url}: {result.get('msg')}")
    return result["data"]


def source_status(blog: str) -> SourceStatus:
    return cast(SourceStatus, data(blog + "/inner/blog/index/status"))


def wait_ready(compose: Sequence[str], service: str, port: int) -> str:
    url = address(compose, service, port)
    for _ in range(90):
        try:
            if request(url + "/actuator/health", timeout=3).get("status") == "UP":
                return url
        except (URLError, TimeoutError, ConnectionError):
            pass
        time.sleep(1)
    raise RuntimeError(f"{service} did not become healthy")


def up(compose: Sequence[str], service: str) -> None:
    _ = subprocess.run([*compose, "up", "-d", "--no-deps", "--pull", "never", service], check=True)


def queue_counts(broker: str) -> dict[str, tuple[int, int]]:
    rows = output("docker", "exec", broker, "rabbitmqctl", "-q", "list_queues",
                  "name", "messages", "consumers").splitlines()
    queues: dict[str, tuple[int, int]] = {}
    for row in rows:
        fields = row.split()
        if len(fields) == 3 and fields[0] in QUEUES:
            queues[fields[0]] = (int(fields[1]), int(fields[2]))
    if set(queues) != QUEUES:
        raise RuntimeError("Missing search queues: " + ", ".join(sorted(QUEUES - queues.keys())))
    return queues


def drain(blog: str, broker: str, timeout: int) -> SourceStatus:
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        status = source_status(blog)
        if not status["readOnly"]:
            raise RuntimeError("The blog service is still writable")
        queues = queue_counts(broker)
        if status["pausedEvents"] or any(count for q, (count, _) in queues.items() if q.endswith(".dlq")):
            raise RuntimeError("Inspect paused outbox events and search dead letters before retrying")
        if not status["readyEvents"] and not any(count for count, _ in queues.values()):
            return status
        time.sleep(2)
    raise RuntimeError("Timed out draining the BLOG outbox and search queues")


def synchronize(args: Arguments) -> None:
    base = ["docker", "compose", "-f", str(args.compose.resolve())]
    infra = ["docker", "compose", "-f", str(args.infra.resolve())]
    broker = container(infra, "rabbitmq")
    blog = wait_ready(base, "micro-base", 8082)
    print("Source:", source_status(blog), flush=True)
    print("Search queues (messages, consumers):", queue_counts(broker), flush=True)
    if not args.apply:
        return
    with tempfile.TemporaryDirectory(dir=args.compose.resolve().parent) as temp:
        override = Path(temp) / "maintenance.json"
        _ = override.write_text(json.dumps({"services": {
            "micro-base": {"environment": {"MEGALITH_BLOG_MAINTENANCE_READ_ONLY": "true"}},
            "micro-search": {"environment": {"MEGALITH_SEARCH_MAINTENANCE_ENABLED": "true"}},
        }}))
        maintenance = [*base, "-f", str(override)]
        up(maintenance, "micro-base")
        blog = wait_ready(base, "micro-base", 8082)
        if args.first_release:
            up(infra, "openobserve")
            observe = address(infra, "openobserve", 5080)
            for _ in range(60):
                try:
                    if request(observe + "/healthz", timeout=3).get("status") == "ok":
                        break
                except (URLError, TimeoutError, ConnectionError):
                    pass
                time.sleep(1)
            else:
                raise RuntimeError("OpenObserve failed to start")
            # Events can accumulate while the legacy search service is offline.
            up(base, "micro-search")
            _ = wait_ready(base, "micro-search", 8085)
        source = drain(blog, broker, args.drain_timeout)
        if "revisionTotal" not in source:
            raise RuntimeError("The blog source must report revisionTotal before the index swap")
        up(maintenance, "micro-search")
        search = wait_ready(base, "micro-search", 8085)
        if any(consumers or messages for messages, consumers in queue_counts(broker).values()):
            raise RuntimeError("Search queues must be empty with all consumers stopped")
        # A lost response may follow a successful swap. Never resubmit this POST automatically.
        result = cast(dict[str, list[RebuiltIndex]],
                      data(search + "/inner/search/index/rebuild", "POST", args.rebuild_timeout))
        indexes = result["indexes"]
        if (len(indexes) != 3 or len({row["index"] for row in indexes}) != 3
                or [row["documents"] for row in indexes] != [source["publishedTotal"], source["total"], source["revisionTotal"]]):
            raise RuntimeError(f"Index count mismatch. Inspect rebuild result: {result}")
        print("Verified rebuilt indexes:", json.dumps(indexes), flush=True)
        up(base, "micro-search")
        _ = wait_ready(base, "micro-search", 8085)
        for _ in range(30):
            queues = queue_counts(broker)
            if all(queues[q][1] for q in ("blog.change.queue.es", "blog.admin.change.queue.es")):
                break
            time.sleep(1)
        else:
            raise RuntimeError("Search consumers have not resumed")
        up(base, "micro-base")
        blog = wait_ready(base, "micro-base", 8082)
        if source_status(blog)["readOnly"]:
            raise RuntimeError("The blog service is still read-only")
        print("Search consumers and blog writes are active.", flush=True)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    _ = parser.add_argument("--compose", type=Path, default=Arguments.compose)
    _ = parser.add_argument("--infra", type=Path, default=Arguments.infra)
    _ = parser.add_argument("--apply", action="store_true", help="enable maintenance and rebuild indexes")
    _ = parser.add_argument("--first-release", action="store_true",
                        help="start OpenObserve and the new search consumers before draining queued events")
    _ = parser.add_argument("--drain-timeout", type=int, default=Arguments.drain_timeout)
    _ = parser.add_argument("--rebuild-timeout", type=int, default=Arguments.rebuild_timeout)
    args = parser.parse_args(namespace=Arguments())
    with (args.compose.resolve().parent / ".sync-search.lock").open("a") as lock:
        fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
        synchronize(args)


if __name__ == "__main__":
    try:
        main()
    except (OSError, RuntimeError, subprocess.CalledProcessError) as error:
        sys.exit(str(error))
