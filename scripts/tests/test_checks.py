import os
import unittest
from unittest.mock import patch

from scripts import checks


class NativeSmokeCheckTest(unittest.TestCase):
    @patch.object(checks, "run")
    @patch.dict(os.environ, {}, clear=True)
    def test_native_smoke_rejects_missing_image_instead_of_skipping_test(self, run):
        with patch("sys.argv", ["checks", "exhibit-native-smoke"]):
            with self.assertRaisesRegex(ValueError, "MICRO_EXHIBIT_NATIVE_IMAGE is required"):
                checks.main()
        run.assert_not_called()


if __name__ == "__main__":
    unittest.main()
