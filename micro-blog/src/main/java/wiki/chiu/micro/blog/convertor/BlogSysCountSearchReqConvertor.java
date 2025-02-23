package wiki.chiu.micro.blog.convertor;

import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;

import java.util.List;

public class BlogSysCountSearchReqConvertor {
    public static BlogSysCountSearchReq convert(BlogDownloadReq downloadReq, Long userId, List<String> roles) {
        return BlogSysCountSearchReq.builder()
                .keywords(downloadReq.keywords())
                .status(downloadReq.status())
                .createEnd(downloadReq.createEnd())
                .createStart(downloadReq.createStart())
                .userId(userId)
                .roles(roles)
                .build();
    }
}
