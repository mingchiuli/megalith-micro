"""Shared helpers for the GitHub Actions entry points."""

from __future__ import annotations

import argparse
import os
import subprocess
import sys
import uuid
from collections.abc import Callable, Mapping, Sequence
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def run(
    *args: str,
    capture_output: bool = False,
    input: str | None = None,
    stdout: int | None = None,
    stderr: int | None = None,
) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        args,
        cwd=ROOT,
        check=True,
        text=True,
        capture_output=capture_output,
        input=input,
        stdout=stdout,
        stderr=stderr,
    )


def write_outputs(values: Mapping[str, str]) -> None:
    with Path(os.environ["GITHUB_OUTPUT"]).open("a", encoding="utf-8") as output:
        for name, value in values.items():
            delimiter = f"output_{uuid.uuid4().hex}"
            _ = output.write(f"{name}<<{delimiter}\n{value}\n{delimiter}\n")


class CommandArguments(argparse.Namespace):
    command: str = ""


def parse_command(description: str | None, commands: Sequence[str]) -> str:
    parser = argparse.ArgumentParser(description=description)
    _ = parser.add_argument("command", choices=commands)
    return parser.parse_args(namespace=CommandArguments()).command


def entrypoint(main: Callable[[], None]) -> None:
    try:
        main()
    except subprocess.CalledProcessError as error:
        sys.exit(error.returncode if error.returncode > 0 else 1)
    except (OSError, ValueError, RuntimeError) as error:
        print(f"::error::{error}", file=sys.stderr)
        sys.exit(1)
