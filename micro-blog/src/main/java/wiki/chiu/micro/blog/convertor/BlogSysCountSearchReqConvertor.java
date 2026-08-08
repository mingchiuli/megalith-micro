package wiki.chiu.micro.blog.convertor;

import java.util.List;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.search.api.req.BlogSysCountSearchReq;

public class BlogSysCountSearchReqConvertor {
  public static BlogSysCountSearchReq convert(
      BlogDownloadReq downloadReq, Long userId, List<String> roles) {
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
