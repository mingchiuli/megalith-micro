package wiki.chiu.micro.search.api;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.search.api.req.BlogReadCountReq;
import wiki.chiu.micro.search.api.req.BlogSysCountSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;
import wiki.chiu.micro.search.api.vo.BlogSearchRpcVo;

public interface SearchHttpService {

    @PostExchange("/blog/search")
    Result<BlogSearchRpcVo> searchBlogs(@RequestBody BlogSysSearchReq req);

    @PostExchange("/blog/count")
    Result<Long> countBlogs(@RequestBody BlogSysCountSearchReq req);

    @PostExchange("/blog/views/batch")
    Result<Void> updateReadCounts(@RequestBody List<BlogReadCountReq> counts);
}
