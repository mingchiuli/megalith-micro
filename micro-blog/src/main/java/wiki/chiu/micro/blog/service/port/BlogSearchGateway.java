package wiki.chiu.micro.blog.service.port;

import wiki.chiu.micro.search.api.req.BlogSysCountSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;
import wiki.chiu.micro.search.api.vo.BlogSearchRpcVo;

public interface BlogSearchGateway {

  BlogSearchRpcVo searchBlogs(BlogSysSearchReq request);

  long countBlogs(BlogSysCountSearchReq request);
}
