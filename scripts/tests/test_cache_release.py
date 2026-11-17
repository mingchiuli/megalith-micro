import os
import unittest
import urllib.error
from unittest.mock import MagicMock, patch

from scripts import cache_release


def metadata_response(version):
    response = MagicMock()
    response.__enter__.return_value = response
    response.status = 200
    response.read.return_value = (
        f"<metadata><versioning><release>{version}</release></versioning></metadata>".encode()
    )
    return response


class CacheReleaseTest(unittest.TestCase):
    @patch.object(cache_release.urllib.request, "urlopen")
    def test_companion_artifact_can_start_at_an_existing_cache_version(self, urlopen):
        urlopen.side_effect = [
            metadata_response("1.1.0"),
            urllib.error.HTTPError(cache_release.NATIVE_HINTS_METADATA_URL, 404, "missing", {}, None),
        ]

        cache_release.verify_release_order("1.2.0")

        self.assertEqual(
            [cache_release.METADATA_URL, cache_release.NATIVE_HINTS_METADATA_URL],
            [call.args[0] for call in urlopen.call_args_list],
        )

    @patch.object(cache_release.urllib.request, "urlopen")
    def test_already_published_companion_version_rejects_the_release(self, urlopen):
        urlopen.side_effect = [metadata_response("1.1.0"), metadata_response("1.2.0")]

        with self.assertRaisesRegex(ValueError, "must be greater"):
            cache_release.verify_release_order("1.2.0")

    @patch.object(cache_release, "run")
    @patch.dict(os.environ, {"CACHE_VERSION": "1.2.0"})
    def test_publish_stages_both_artifacts_before_finalization(self, run):
        with patch("sys.argv", ["cache_release", "publish"]):
            cache_release.main()

        run.assert_called_once_with(
            "./gradlew",
            ":common-native-hints:publishMavenJavaPublicationToCentralStagingRepository",
            ":cache:publishMavenJavaPublicationToCentralStagingRepository",
            "-PcacheVersion=1.2.0",
        )


if __name__ == "__main__":
    unittest.main()
