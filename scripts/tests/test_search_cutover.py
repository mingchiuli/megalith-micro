import os
import subprocess
import sys
import tempfile
import unittest
from collections.abc import Generator
from contextlib import contextmanager
from pathlib import Path
from unittest.mock import Mock, patch

from scripts import deploy

REBUILD = (
    sys.executable, "megalith-ops/sync_search.py", "--compose", "docker-compose.yml",
    "--apply", "--first-release",
)


@contextmanager
def pending_cutover() -> Generator[Path, None, None]:
    with tempfile.TemporaryDirectory() as directory:
        old_dir = Path.cwd()
        os.chdir(directory)
        try:
            Path("docker-compose.yml").touch()
            marker = Path("megalith-ops/search-cutover.pending")
            marker.parent.mkdir()
            marker.touch()
            yield marker
        finally:
            os.chdir(old_dir)


class SearchCutoverTest(unittest.TestCase):
    def test_successful_rebuild_completes_one_time_cutover(self) -> None:
        command = Mock(return_value=subprocess.CompletedProcess([], 0, stdout="old-image"))
        with pending_cutover() as marker, patch.object(deploy, "run", command):
            deploy.deploy_service("micro-search", "test")
            command.assert_called_with(*REBUILD)
            self.assertFalse(marker.exists())

    def test_failed_rebuild_keeps_marker_for_recovery(self) -> None:
        def fail_rebuild(*args: str, **_kwargs: object) -> subprocess.CompletedProcess[str]:
            if args == REBUILD:
                raise subprocess.CalledProcessError(1, args)
            return subprocess.CompletedProcess(args, 0, stdout="old-image")

        with pending_cutover() as marker:
            with patch.object(deploy, "run", fail_rebuild), self.assertRaises(subprocess.CalledProcessError):
                deploy.deploy_service("micro-search", "test")
            self.assertTrue(marker.is_file())
