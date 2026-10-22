package wiki.chiu.micro.search.api;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.result.Result;
import wiki.chiu.micro.search.api.req.BlogReadCountReq;
import wiki.chiu.micro.search.api.req.BlogRevisionSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysCountSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;
import wiki.chiu.micro.search.api.vo.AdminBlogSearchRpcVo;
import wiki.chiu.micro.search.api.vo.AdminRevisionSearchRpcVo;
import wiki.chiu.micro.search.api.vo.BlogSearchRpcVo;
import wiki.chiu.micro.search.api.vo.PublicBlogRpcVo;
import wiki.chiu.micro.search.api.vo.RecommendationRpcVo;

public interface SearchHttpService {

    @PostExchange("/blog/search")
    Result<BlogSearchRpcVo> searchBlogs(@RequestBody BlogSysSearchReq req);

    @PostExchange("/blog/admin/search")
    Result<AdminBlogSearchRpcVo> searchAdminBlogs(@RequestBody BlogSysSearchReq req);

    @PostExchange("/blog/revisions/search")
    Result<AdminRevisionSearchRpcVo> searchRevisions(@RequestBody BlogRevisionSearchReq req);

    @PostExchange("/blog/count")
    Result<Long> countBlogs(@RequestBody BlogSysCountSearchReq req);

    @PostExchange("/blog/views/batch")
    Result<Void> updateReadCounts(@RequestBody List<BlogReadCountReq> counts);

    @GetExchange("/blog/public/page")
    Result<PageAdapter<PublicBlogRpcVo>> publicPage(
        @RequestParam Integer page, @RequestParam Integer size);

    @GetExchange("/blog/public/{blogId}")
    Result<PublicBlogRpcVo> publicBlog(@PathVariable Long blogId);

    @GetExchange("/blog/public/recommendations")
    Result<RecommendationRpcVo> recommendations(
        @RequestParam(required = false) Long blogId, @RequestParam Integer limit);
}
