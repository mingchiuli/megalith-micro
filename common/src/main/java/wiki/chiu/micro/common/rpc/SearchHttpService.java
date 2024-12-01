package wiki.chiu.micro.common.rpc;



import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PostExchange;
import wiki.chiu.micro.common.dto.BlogSearchRpcDto;
import wiki.chiu.micro.common.lang.Result;
import org.springframework.web.service.annotation.GetExchange;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;

public interface SearchHttpService {

    @GetExchange("/blog/search")
    Result<BlogSearchRpcDto> searchBlogs(BlogSysSearchReq req);

    @GetExchange("/blog/count")
    Result<Long> countBlogs(@RequestBody BlogSysCountSearchReq req);

    @PostExchange("/blog/read")
    Result<Void> addReadCount(@RequestParam Long id);
}
