import os
import unittest
import urllib.error
from email.message import Message
from io import BytesIO
from unittest.mock import Mock, call, patch

from scripts import cache_release


class MetadataResponse(BytesIO):
    status: int = 200


def metadata_response(version: str) -> MetadataResponse:
    xml = f"<metadata><versioning><release>{version}</release></versioning></metadata>"
    return MetadataResponse(xml.encode())


class CacheReleaseTest(unittest.TestCase):
    @patch("urllib.request.urlopen")
    def test_companion_artifact_can_start_at_an_existing_cache_version(self, urlopen: Mock) -> None:
        urlopen.side_effect = [
            metadata_response("1.1.0"),
            urllib.error.HTTPError(cache_release.NATIVE_HINTS_METADATA_URL, 404, "missing", Message(), None),
        ]
        cache_release.verify_release_order("1.2.0")

        self.assertEqual(urlopen.call_args_list, [
            call(cache_release.METADATA_URL, timeout=60),
            call(cache_release.NATIVE_HINTS_METADATA_URL, timeout=60),
        ])

    @patch("urllib.request.urlopen")
    def test_already_published_companion_version_rejects_the_release(self, urlopen: Mock) -> None:
        urlopen.side_effect = [metadata_response("1.1.0"), metadata_response("1.2.0")]
        with self.assertRaisesRegex(ValueError, "must be greater"):
            cache_release.verify_release_order("1.2.0")

    @patch.object(cache_release, "run")
    def test_publish_stages_both_artifacts_before_finalization(self, run: Mock) -> None:
        with patch.dict(os.environ, {"CACHE_VERSION": "1.2.0"}), patch(
            "sys.argv", ["cache_release", "publish"]
        ):
            cache_release.main()

        run.assert_called_once_with(
            "./gradlew",
            ":common-native-hints:publishMavenJavaPublicationToCentralStagingRepository",
            ":cache:publishMavenJavaPublicationToCentralStagingRepository",
            "-PcacheVersion=1.2.0",
        )


if __name__ == "__main__":
    _ = unittest.main()
