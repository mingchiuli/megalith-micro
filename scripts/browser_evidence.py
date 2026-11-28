"""Export browser results without credentials and optionally check the full test manifest."""

from __future__ import annotations

import argparse
import csv
import json
from collections import Counter
from pathlib import Path


def records(suite: dict, prefix: tuple[str, ...] = ()):
    path = (*prefix, suite["title"]) if suite.get("title") else prefix
    for spec in suite.get("specs", []):
        for case in spec["tests"]:
            results = case.get("results", [])
            yield {
                "file": spec["file"],
                "case": " / ".join((*path, spec["title"])),
                "browser": case["projectName"],
                "status": results[-1]["status"] if results else "not-run",
                "attempts": len(results),
                "duration_ms": sum(result["duration"] for result in results),
            }
    for child in suite.get("suites", []):
        yield from records(child, path)


def case_key(record: dict) -> tuple[str, str, str]:
    return record["browser"], record["file"], record["case"]


def report_rows(path: Path) -> list[dict]:
    report = json.loads(path.read_text(encoding="utf-8"))
    if report.get("errors"):
        raise ValueError(f"Playwright reported a runner error in {path.name}")
    return [row for suite in report["suites"] for row in records(suite)]


def verify(rows: list[dict], manifest: list[dict]) -> None:
    expected = Counter(map(case_key, manifest))
    actual = Counter(map(case_key, rows))
    if not expected or actual != expected or any(count != 1 for count in actual.values()):
        raise ValueError(f"Browser manifest mismatch: {sum((expected - actual).values())} missing, "
                         f"{sum((actual - expected).values())} extra; each case must occur once")
    failures = [row for row in rows if row["status"] != "passed" or row["attempts"] != 1]
    if failures:
        raise ValueError(f"{len(failures)} browser cases failed, were skipped, or required a retry")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("reports", type=Path, nargs="+")
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--manifest", type=Path, help="Unfiltered Playwright --list JSON for this suite")
    args = parser.parse_args()
    rows = [row for report in args.reports for row in report_rows(report)]
    if args.manifest:
        verify(rows, report_rows(args.manifest))
    fields = ("file", "case", "browser", "status", "attempts", "duration_ms")
    with args.output.open("w", newline="", encoding="utf-8") as stream:
        writer = csv.DictWriter(stream, fieldnames=fields, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)
