package wiki.chiu.micro.search.adapter.in.http;

import org.springframework.web.servlet.function.ServerRequest;
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.search.api.req.BlogSysCountSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;

public final class SearchRequestConvertor {

  private static final ValidatedRequest v = new ValidatedRequest();

  private SearchRequestConvertor() {}

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

  public static BlogSysCountSearchReq toBlogSysCountSearchReq(ServerRequest request)
      throws Exception {
    BlogSysCountSearchReq req = request.body(BlogSysCountSearchReq.class);

    v.maxLength(req.keywords(), 20, "keywords");
    v.range(req.status(), 0, 3, "status");
    v.notNull(req.userId(), "userId");
    v.nonNegative(req.userId(), "userId");
    v.notNull(req.allData(), "allData");
    v.dateRange(req.createStart(), req.createEnd(), "createStart", "createEnd");

    return req;
  }
}
