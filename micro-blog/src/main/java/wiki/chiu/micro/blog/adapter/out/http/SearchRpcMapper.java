package wiki.chiu.micro.blog.adapter.out.http;

import wiki.chiu.micro.blog.application.model.BlogSearchQuery;
import wiki.chiu.micro.blog.application.model.BlogSearchResult;
import wiki.chiu.micro.search.api.req.BlogSysCountSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;
import wiki.chiu.micro.search.api.vo.BlogSearchRpcVo;

/**
 * Translates between the search service payloads and this service's own search model.
 */
public final class SearchRpcMapper {

    private SearchRpcMapper() {
    }

    public static BlogSysSearchReq toSearchReq(BlogSearchQuery query) {
        return BlogSysSearchReq.builder()
            .page(query.currentPage())
            .pageSize(query.pageSize())
            .status(query.selection().status())
            .keywords(query.keywords())
            .createStart(query.selection().createStart())
            .createEnd(query.selection().createEnd())
            .userId(query.selection().userId())
            .allData(query.selection().allData())
            .build();
    }

    public static BlogSysCountSearchReq toCountReq(BlogSearchQuery query) {
        return BlogSysCountSearchReq.builder()
            .keywords(query.keywords())
            .status(query.selection().status())
            .createStart(query.selection().createStart())
            .createEnd(query.selection().createEnd())
            .userId(query.selection().userId())
            .allData(query.selection().allData())
            .build();
    }

    public static BlogSearchResult toResult(BlogSearchRpcVo result) {
        return new BlogSearchResult(result.total(), result.currentPage(), result.size(), result.ids());
    }
}
