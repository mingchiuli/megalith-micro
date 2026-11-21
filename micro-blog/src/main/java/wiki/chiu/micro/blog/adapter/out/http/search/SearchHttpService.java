package wiki.chiu.micro.blog.adapter.out.http.search;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import wiki.chiu.micro.common.result.Result;

public interface SearchHttpService {

    @PostExchange("/blog/admin/search")
    Result<AdminBlogSearchRpcVo> searchAdminBlogs(@RequestBody BlogSysSearchReq req);

    @PostExchange("/blog/revisions/search")
    Result<AdminRevisionSearchRpcVo> searchRevisions(@RequestBody BlogRevisionSearchReq req);

    @PostExchange("/blog/views/batch")
    Result<Void> updateReadCounts(@RequestBody List<BlogReadCountReq> counts);
}
