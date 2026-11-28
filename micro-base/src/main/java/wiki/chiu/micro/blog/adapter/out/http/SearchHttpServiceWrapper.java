package wiki.chiu.micro.blog.adapter.out.http;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.adapter.out.http.search.AdminBlogSearchRpcVo;
import wiki.chiu.micro.blog.adapter.out.http.search.AdminRevisionSearchRpcVo;
import wiki.chiu.micro.blog.adapter.out.http.search.SearchHttpService;
import wiki.chiu.micro.blog.application.model.BlogListItem;
import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.blog.application.model.BlogSearchQuery;
import wiki.chiu.micro.blog.application.model.BlogVersionSummary;
import wiki.chiu.micro.blog.application.model.Page;
import wiki.chiu.micro.blog.application.port.out.BlogSearchGateway;
import wiki.chiu.micro.blog.application.port.out.BlogStatisticsGateway;
import wiki.chiu.micro.common.rpc.RemoteResult;

@Component
public class SearchHttpServiceWrapper implements BlogSearchGateway, BlogStatisticsGateway {

    private final SearchHttpService searchHttpService;

    public SearchHttpServiceWrapper(SearchHttpService searchHttpService) {
        this.searchHttpService = searchHttpService;
    }

    @Override
    public Page<BlogListItem> searchAdmin(BlogSearchQuery query) {
        AdminBlogSearchRpcVo result =
            RemoteResult.requireSuccess(
                () -> searchHttpService.searchAdminBlogs(SearchRpcConvertor.toSearchReq(query)));
        return SearchRpcConvertor.toBlogPage(result);
    }

    @Override
    public Page<BlogVersionSummary> searchVersions(
        Long blogId,
        int page,
        int pageSize,
        String keywords,
        long userId,
        boolean allData) {
        AdminRevisionSearchRpcVo result =
            RemoteResult.requireSuccess(
                () ->
                    searchHttpService.searchRevisions(
                        SearchRpcConvertor.toRevisionSearchReq(
                            blogId, page, pageSize, keywords, userId, allData)));
        return SearchRpcConvertor.toVersionPage(result);
    }

    @Override
    public void updateReadCounts(List<BlogReadCount> counts) {
        RemoteResult.requireSuccess(
            () ->
                searchHttpService.updateReadCounts(
                    SearchRpcConvertor.toReadCountRequests(counts)));
    }
}
