import os
import unittest
from unittest.mock import Mock, patch

from scripts import checks


class NativeSmokeCheckTest(unittest.TestCase):
    @patch.object(checks, "run")
    def test_native_smoke_rejects_missing_image_instead_of_skipping_test(self, run: Mock) -> None:
        with patch.dict(os.environ, {}, clear=True), patch("sys.argv", ["checks", "exhibit-native-smoke"]), self.assertRaisesRegex(
            ValueError, "MICRO_EXHIBIT_NATIVE_IMAGE is required"
        ):
            checks.main()
        run.assert_not_called()


if __name__ == "__main__":
    _ = unittest.main()
