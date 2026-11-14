"""Manage the runner's temporary WireGuard connection."""

import os
import subprocess

from .common import entrypoint, parse_command, run


def connect() -> None:
    config = os.environ["WIREGUARD_CONFIG"]
    _ = run("sudo", "apt-get", "update")
    _ = run("sudo", "apt-get", "install", "--yes", "wireguard-tools")
    _ = run("sudo", "install", "-d", "-m", "700", "/etc/wireguard")
    _ = run(
        "sudo",
        "tee",
        "/etc/wireguard/wg0.conf",
        input=f"{config}\n",
        stdout=subprocess.DEVNULL,
    )
    _ = run("sudo", "chmod", "600", "/etc/wireguard/wg0.conf")
    _ = run("sudo", "wg-quick", "up", "wg0")


def disconnect() -> None:
    try:
        _ = subprocess.run(["sudo", "wg-quick", "down", "wg0"], check=False)
    finally:
        _ = run("sudo", "rm", "-f", "/etc/wireguard/wg0.conf")


def main() -> None:
    if parse_command(__doc__, ("connect", "disconnect")) == "connect":
        connect()
    else:
        disconnect()


if __name__ == "__main__":
    entrypoint(main)
