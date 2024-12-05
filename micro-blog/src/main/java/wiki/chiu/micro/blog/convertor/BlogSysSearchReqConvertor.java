package wiki.chiu.micro.blog.convertor;

import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;

public class BlogSysSearchReqConvertor {
    public static BlogSysSearchReq convert(BlogQueryReq blogQueryReq) {
        return BlogSysSearchReq.builder()
                .page(blogQueryReq.currentPage())
                .pageSize(blogQueryReq.size())
                .status(blogQueryReq.status())
                .keywords(blogQueryReq.keywords())
                .createStart(blogQueryReq.createStart())
                .createEnd(blogQueryReq.createEnd())
                .build();
    }

    public static BlogSysSearchReq convert(BlogDownloadReq downloadReq, Integer page, Integer pagSize) {
       return BlogSysSearchReq.builder()
                .page(page)
                .pageSize(pagSize)
                .keywords(downloadReq.keywords())
                .status(downloadReq.status())
                .createStart(downloadReq.createStart())
                .createEnd(downloadReq.createEnd())
                .build();
    }
}
