package wiki.chiu.micro.blog.convertor;

import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;

public class BlogSysCountSearchReqConvertor {
    public static BlogSysCountSearchReq convert(BlogDownloadReq blogDownloadReq) {

        return BlogSysCountSearchReq.builder()
                .keywords(blogDownloadReq.keywords())
                .createEnd(blogDownloadReq.createEnd())
                .createEnd(blogDownloadReq.createEnd())
                .build();
    }
}
