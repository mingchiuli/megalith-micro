package wiki.chiu.micro.search.adapter.in.http;

import java.util.List;

import org.springframework.web.servlet.function.ServerRequest;

import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.search.api.req.BlogReadCountReq;
import wiki.chiu.micro.search.api.req.BlogRevisionSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;
import wiki.chiu.micro.search.application.model.BlogReadCount;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.RevisionSearchQuery;

public final class SearchRequestConvertor {

    private static final ValidatedRequest v = new ValidatedRequest();

    private SearchRequestConvertor() {
    }

    public static BlogSysSearchReq toBlogSysSearchReq(ServerRequest request) throws Exception {
        BlogSysSearchReq req = request.body(BlogSysSearchReq.class);

        v.notNull(req.page(), "page");
        v.positive(req.page(), "page");
        v.notNull(req.pageSize(), "pageSize");
        v.positive(req.pageSize(), "pageSize");
        v.maxLength(req.keywords(), 20, "keywords");
        v.range(req.status(), 0, 3, "status");
        v.notNull(req.userId(), "userId");
        v.nonNegative(req.userId(), "userId");
        v.notNull(req.allData(), "allData");
        v.dateRange(req.createStart(), req.createEnd(), "createStart", "createEnd");

        return req;
    }

    public static PrivateBlogSearchQuery toQuery(BlogSysSearchReq req) {
        return new PrivateBlogSearchQuery(
            req.page(),
            req.pageSize(),
            req.keywords(),
            req.status(),
            req.createStart(),
            req.createEnd(),
            req.userId(),
            req.allData(),
            Boolean.TRUE.equals(req.draftOnly()));
    }

    public static RevisionSearchQuery toQuery(BlogRevisionSearchReq req) {
        return new RevisionSearchQuery(req.blogId(), req.page(), req.pageSize(), req.keywords(),
            req.userId(), Boolean.TRUE.equals(req.allData()));
    }

    public static List<BlogReadCount> toReadCounts(List<BlogReadCountReq> counts) {
        return counts.stream().map(count -> new BlogReadCount(count.blogId(), count.readCount())).toList();
    }
}
