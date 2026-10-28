package wiki.chiu.micro.blog.api;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;
import wiki.chiu.micro.common.model.BlogAdminIndexSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.common.result.Result;

public interface BlogIndexSourceHttpService {

    @GetExchange("/blog/index/status")
    Result<BlogIndexSourceStatus> indexSourceStatus();

    @GetExchange("/blog/index/snapshots")
    Result<List<BlogSnapshot>> indexSnapshots(@RequestParam long afterId, @RequestParam int limit);

    @GetExchange("/blog/index/bootstrap-snapshots")
    Result<List<BlogSnapshot>> indexBootstrapSnapshots(
        @RequestParam("afterId") long afterId, @RequestParam("limit") int limit);

    @GetExchange("/blog/index/admin-snapshots")
    Result<List<BlogAdminIndexSnapshot>> indexAdminSnapshots(
        @RequestParam("afterId") long afterId, @RequestParam("limit") int limit);

    @GetExchange("/blog/index/revision-snapshots")
    Result<List<BlogRevisionSnapshot>> indexRevisionSnapshots(
        @RequestParam("afterRevisionId") long afterRevisionId,
        @RequestParam("limit") int limit);
}
