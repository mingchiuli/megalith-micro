package wiki.chiu.micro.blog.converter;

import org.springframework.web.servlet.function.ServerRequest;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.req.SensitiveContentReq;
import wiki.chiu.micro.common.web.ValidatedRequest;

import java.time.LocalDateTime;
import java.util.function.Function;

import static wiki.chiu.micro.common.lang.Const.URL_REGEX;
import static wiki.chiu.micro.common.web.FunctionalWeb.nullableParam;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;

public final class BlogRequestConverter {

    private static final ValidatedRequest v = new ValidatedRequest();

    private BlogRequestConverter() {}

    public static BlogQueryReq toBlogQueryReq(ServerRequest request) {
        return new BlogQueryReq(
                v.positive(requiredParam(request, "currentPage", Integer::valueOf), "currentPage"),
                v.positive(requiredParam(request, "size", Integer::valueOf), "size"),
                v.maxLength(nullableParam(request, "keywords", Function.identity()), 20, "keywords"),
                v.range(nullableParam(request, "status", Integer::valueOf), 0, 3, "status"),
                nullableParam(request, "createStart", LocalDateTime::parse),
                nullableParam(request, "createEnd", LocalDateTime::parse));
    }

    public static BlogDownloadReq toBlogDownloadReq(ServerRequest request) {
        return new BlogDownloadReq(
                v.maxLength(nullableParam(request, "keywords", Function.identity()), 20, "keywords"),
                v.range(nullableParam(request, "status", Integer::valueOf), 0, 3, "status"),
                nullableParam(request, "createStart", LocalDateTime::parse),
                nullableParam(request, "createEnd", LocalDateTime::parse));
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
            if (item.startIndex() != null && item.endIndex() != null) {
                if (item.startIndex() < 0 || item.startIndex() >= item.endIndex()
                        || item.endIndex() > req.content().length()) {
                    throw new IllegalArgumentException("sensitiveContent start/end indices invalid");
                }
            }
        }
        return req;
    }
}
