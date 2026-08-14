package wiki.chiu.micro.blog.convertor;

import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;

public class BlogSysSearchReqConvertor {
  public static BlogSysSearchReq convert(BlogQueryReq blogQueryReq, Long userId, boolean allData) {
    return BlogSysSearchReq.builder()
        .page(blogQueryReq.currentPage())
        .pageSize(blogQueryReq.size())
        .status(blogQueryReq.status())
        .keywords(blogQueryReq.keywords())
        .createStart(blogQueryReq.createStart())
        .createEnd(blogQueryReq.createEnd())
        .userId(userId)
        .allData(allData)
        .build();
  }

  public static BlogSysSearchReq convert(
      BlogDownloadReq downloadReq, Integer page, Integer pagSize, Long userId, boolean allData) {
    return BlogSysSearchReq.builder()
        .page(page)
        .pageSize(pagSize)
        .keywords(downloadReq.keywords())
        .status(downloadReq.status())
        .createStart(downloadReq.createStart())
        .createEnd(downloadReq.createEnd())
        .userId(userId)
        .allData(allData)
        .build();
  }
}
