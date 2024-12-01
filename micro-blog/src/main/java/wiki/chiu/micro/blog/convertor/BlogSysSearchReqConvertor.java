package wiki.chiu.micro.blog.convertor;

import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;

public class BlogSysSearchReqConvertor {
    public static BlogSysSearchReq convert(BlogQueryReq blogQueryReq) {
        return BlogSysSearchReq.builder()
                .page(blogQueryReq.currentPage())
                .pageSize(blogQueryReq.size())
                .keywords(blogQueryReq.keywords())
                .createStart(blogQueryReq.createStart())
                .createEnd(blogQueryReq.createEnd())
                .build();
    }
}
