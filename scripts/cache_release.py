"""Validate and publish the cache artifact to Maven Central."""

import base64
import os
import re
import urllib.error
import urllib.parse
import urllib.request
import xml.etree.ElementTree as ET
from http.client import HTTPResponse
from typing import cast

from .common import entrypoint, parse_command, run, write_outputs

METADATA_URL = (
    "https://repo1.maven.org/maven2/wiki/chiu/megalith/"
    "megalith-cache-spring-boot-starter/maven-metadata.xml"
)
VERSION_PATTERN = re.compile(
    r"(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)", re.ASCII
)


def parse_version(value: str) -> tuple[int, ...]:
    if not VERSION_PATTERN.fullmatch(value):
        raise ValueError(f"Invalid release version: {value}")
    return tuple(int(part) for part in value.split("."))


def resolve_version(tag: str) -> str:
    if not tag.startswith("cache-v"):
        raise ValueError(
            "Tag must use cache-v<major>.<minor>.<patch> with no leading zeroes."
        )
    version = tag.removeprefix("cache-v")
    _ = parse_version(version)
    return version


def verify_release_order(version: str) -> None:
    current = parse_version(version)
    try:
        with cast(
            HTTPResponse, urllib.request.urlopen(METADATA_URL, timeout=60)
        ) as response:
            if response.status != 200:
                raise ValueError(
                    f"Maven Central metadata request returned HTTP {response.status}."
                )
            metadata = response.read()
    except urllib.error.HTTPError as error:
        if error.code != 404:
            raise ValueError(
                f"Maven Central metadata request returned HTTP {error.code}."
            ) from error
        if version != "0.1.0":
            raise ValueError(
                f"The first release of the new artifact must be 0.1.0, not {version}."
            )
        print("No existing artifact metadata found; allowing initial 0.1.0 release.")
        return
    try:
        latest = ET.fromstring(metadata).findtext("./versioning/release", default="")
    except ET.ParseError as error:
        raise ValueError("Maven Central returned invalid metadata XML.") from error
    if current <= parse_version(latest):
        raise ValueError(
            f"Release {version} must be greater than Maven Central version {latest}."
        )


def finalize_release() -> None:
    credentials = f"{os.environ['CENTRAL_USERNAME']}:{os.environ['CENTRAL_PASSWORD']}"
    token = base64.b64encode(credentials.encode()).decode("ascii")
    print(f"::add-mask::{token}", flush=True)
    namespace = urllib.parse.quote(os.environ["CENTRAL_NAMESPACE"], safe="")
    request = urllib.request.Request(
        f"https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/{namespace}?publishing_type=automatic",
        headers={"Authorization": f"Bearer {token}"},
        method="POST",
    )
    try:
        with cast(
            HTTPResponse, urllib.request.urlopen(request, timeout=60)
        ) as response:
            print(response.read().decode("utf-8", errors="replace"))
    except urllib.error.HTTPError as error:
        raise ValueError(
            f"Maven Central finalization returned HTTP {error.code}."
        ) from error


def main() -> None:
    command = parse_command(
        __doc__, ("resolve-version", "verify-order", "check", "publish", "finalize")
    )
    if command == "resolve-version":
        write_outputs({"version": resolve_version(os.environ["GITHUB_REF_NAME"])})
    elif command == "verify-order":
        verify_release_order(os.environ["CACHE_VERSION"])
    elif command == "finalize":
        finalize_release()
    else:
        version = os.environ["CACHE_VERSION"]
        _ = parse_version(version)
        tasks = (
            (":cache:check", ":cache:integrationTest")
            if command == "check"
            else (":cache:publishMavenJavaPublicationToCentralStagingRepository",)
        )
        _ = run("./gradlew", *tasks, f"-PcacheVersion={version}")


if __name__ == "__main__":
    entrypoint(main)
