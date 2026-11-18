import re
import subprocess
import unittest
from pathlib import Path
from unittest.mock import patch

from scripts import services


class ServiceSelectionTest(unittest.TestCase):
    @patch.object(services, "run")
    def test_pr_uses_merge_base_and_head(self, run):
        run.side_effect = [None, None, subprocess.CompletedProcess([], 0, "ancestor\n"),
                           None, subprocess.CompletedProcess([], 0, "micro-frontend/src/App.vue\0")]
        result = services.pull_request_services({"pull_request": {
            "base": {"sha": "base"}, "head": {"sha": "head"}}})
        self.assertEqual({services.FRONTEND}, result)
        self.assertEqual(("git", "merge-base", "base", "head"), run.call_args_list[2].args)
        self.assertEqual("ancestor", run.call_args_list[-1].args[-2])
        self.assertEqual("head", run.call_args_list[-1].args[-1])

    @patch.object(services, "run", side_effect=subprocess.CalledProcessError(1, "git"))
    def test_missing_commit_checks_every_service(self, run):
        self.assertEqual(set(services.SERVICE_ORDER), services.pull_request_services({"pull_request": {
            "base": {"sha": "base"}, "head": {"sha": "head"}}}))
        self.assertEqual(set(services.SERVICE_ORDER), services.changed_services("missing", "head"))

    def test_database_detail_provider_and_consumer_deploy_before_old_search_route_is_removed(self):
        selected = {"micro-blog", "micro-exhibit", "micro-search"}
        self.assertEqual("micro-blog micro-exhibit micro-search", services.service_outputs(selected)["deploy-services"])

    def test_shared_java_change_checks_java_and_docs_do_not_deploy(self):
        self.assertEqual(set(services.JAVA_SERVICES), services.services_for_paths(["cache/src/main/Foo.java"]))
        self.assertEqual(set(), services.services_for_paths(["README.md"]))
        self.assertEqual(set(services.JAVA_SERVICES), services.services_for_paths([
            "common-native-hints/src/main/java/wiki/chiu/micro/common/nativehints/config/NettyRuntimeHints.java"
        ]))

    def test_pr_cannot_enter_any_publish_or_deploy_job(self):
        workflow = Path(".github/workflows/main-services.yml").read_text()
        for name in ("build-java", "build-rust", "build-frontend", "deploy"):
            job = re.split(r"\n  (?=\S)", workflow.split(f"\n  {name}:", 1)[1])[0]
            # The guard precedes each job's first step, including the multiline deploy condition.
            self.assertIn("github.event_name != 'pull_request'", job.split("    steps:", 1)[0])


if __name__ == "__main__":
    unittest.main()
