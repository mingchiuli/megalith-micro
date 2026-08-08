package wiki.chiu.micro.blog.converter;

import static wiki.chiu.micro.common.lang.Const.URL_REGEX;
import static wiki.chiu.micro.common.web.FunctionalWeb.nullableParam;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;

import java.time.LocalDateTime;
import java.util.function.Function;
import org.springframework.web.servlet.function.ServerRequest;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.req.SensitiveContentReq;
import wiki.chiu.micro.common.lang.SensitiveTypeEnum;
import wiki.chiu.micro.common.web.ValidatedRequest;

public final class BlogRequestConverter {

  private static final ValidatedRequest v = new ValidatedRequest();

  private BlogRequestConverter() {}

  public static BlogQueryReq toBlogQueryReq(ServerRequest request) {
    BlogQueryReq query =
        new BlogQueryReq(
            v.positive(requiredParam(request, "currentPage", Integer::valueOf), "currentPage"),
            v.positive(requiredParam(request, "size", Integer::valueOf), "size"),
            v.maxLength(nullableParam(request, "keywords", Function.identity()), 20, "keywords"),
            v.range(nullableParam(request, "status", Integer::valueOf), 0, 3, "status"),
            nullableParam(request, "createStart", LocalDateTime::parse),
            nullableParam(request, "createEnd", LocalDateTime::parse));
    v.dateRange(query.createStart(), query.createEnd(), "createStart", "createEnd");
    return query;
  }

  public static BlogDownloadReq toBlogDownloadReq(ServerRequest request) {
    BlogDownloadReq download =
        new BlogDownloadReq(
            v.maxLength(nullableParam(request, "keywords", Function.identity()), 20, "keywords"),
            v.range(nullableParam(request, "status", Integer::valueOf), 0, 3, "status"),
            nullableParam(request, "createStart", LocalDateTime::parse),
            nullableParam(request, "createEnd", LocalDateTime::parse));
    v.dateRange(download.createStart(), download.createEnd(), "createStart", "createEnd");
    return download;
  }

  public static BlogEntityReq toBlogEntityReq(ServerRequest request) throws Exception {
    BlogEntityReq req = request.body(BlogEntityReq.class);

    v.notNull(req.id(), "id");
    req.id().ifPresent(id -> v.positive(id, "id"));
    v.notBlank(req.title(), "title");
    v.notBlank(req.description(), "description");
    v.notBlank(req.content(), "content");
    v.notNull(req.status(), "status");
    v.range(req.status(), 0, 3, "status");
    v.notNull(req.link(), "link");
    if (!req.link().isEmpty() && !req.link().matches(URL_REGEX)) {
      throw new IllegalArgumentException("link is invalid");
    }
    v.notNull(req.sensitiveContentList(), "sensitiveContentList");
    for (SensitiveContentReq item : req.sensitiveContentList()) {
      v.notNull(item, "sensitiveContentList element");
      v.notNull(item.type(), "sensitiveContent type");
      if (!SensitiveTypeEnum.SENSITIVE_TYPE_SET.contains(item.type())) {
        throw new IllegalArgumentException("sensitiveContent type is invalid");
      }
      if ((item.startIndex() == null) != (item.endIndex() == null)) {
        throw new IllegalArgumentException("sensitiveContent start/end indices invalid");
      }
      if (item.startIndex() != null) {
        String source = sensitiveSource(req, item.type());
        if (item.startIndex() < 0
            || item.startIndex() >= item.endIndex()
            || item.endIndex() > source.length()) {
          throw new IllegalArgumentException("sensitiveContent start/end indices invalid");
        }
      }
    }
    return req;
  }

  private static String sensitiveSource(BlogEntityReq req, Integer type) {
    if (SensitiveTypeEnum.TITLE.getCode().equals(type)) {
      return req.title();
    }
    if (SensitiveTypeEnum.DESCRIPTION.getCode().equals(type)) {
      return req.description();
    }
    return req.content();
  }
}
