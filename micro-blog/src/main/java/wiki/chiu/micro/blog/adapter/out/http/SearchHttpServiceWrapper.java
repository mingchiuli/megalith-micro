package wiki.chiu.micro.blog.adapter.out.http;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.blog.application.model.BlogSearchQuery;
import wiki.chiu.micro.blog.application.model.BlogSearchResult;
import wiki.chiu.micro.blog.application.port.out.BlogSearchGateway;
import wiki.chiu.micro.blog.application.port.out.BlogStatisticsGateway;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.search.api.SearchHttpService;
import wiki.chiu.micro.search.api.req.BlogReadCountReq;

@Component
public class SearchHttpServiceWrapper implements BlogSearchGateway, BlogStatisticsGateway {

    private final SearchHttpService searchHttpService;

    public SearchHttpServiceWrapper(SearchHttpService searchHttpService) {
        this.searchHttpService = searchHttpService;
    }

    @Override
    public BlogSearchResult searchBlogs(BlogSearchQuery query) {
        return SearchRpcMapper.toResult(
            RemoteResult.requireSuccess(
                () -> searchHttpService.searchBlogs(SearchRpcMapper.toSearchReq(query))));
    }

    @Override
    public long countBlogs(BlogSearchQuery query) {
        return RemoteResult.requireSuccess(
            () -> searchHttpService.countBlogs(SearchRpcMapper.toCountReq(query)));
    }

    @Override
    public void updateReadCounts(List<BlogReadCount> counts) {
        RemoteResult.requireSuccess(
            () ->
                searchHttpService.updateReadCounts(
                    counts.stream()
                        .map(count -> new BlogReadCountReq(count.blogId(), count.readCount()))
                        .toList()));
    }
}
