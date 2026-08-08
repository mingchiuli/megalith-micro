package wiki.chiu.micro.search.converter;

import org.springframework.web.servlet.function.ServerRequest;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;
import wiki.chiu.micro.common.web.ValidatedRequest;

public final class SearchRequestConverter {

  private static final ValidatedRequest v = new ValidatedRequest();

  private SearchRequestConverter() {}

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
    v.notNull(req.roles(), "roles");
    for (String role : req.roles()) {
      v.notBlank(role, "roles element");
    }
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
    v.notNull(req.roles(), "roles");
    for (String role : req.roles()) {
      v.notBlank(role, "roles element");
    }
    v.dateRange(req.createStart(), req.createEnd(), "createStart", "createEnd");

    return req;
  }
}
