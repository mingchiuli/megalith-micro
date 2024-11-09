package wiki.chiu.micro.common.rpc;



import org.springframework.web.bind.annotation.RequestParam;
import wiki.chiu.micro.common.dto.BlogSearchRpcDto;
import wiki.chiu.micro.common.lang.Result;
import org.springframework.web.service.annotation.GetExchange;

public interface SearchHttpService {

    @GetExchange("/blog/search")
    Result<BlogSearchRpcDto> searchBlogs(@RequestParam(required = false) Integer currentPage, @RequestParam(required = false) Integer size, @RequestParam(required = false) String keywords);

    @GetExchange("/blog/count")
    Result<Long> countBlogs(@RequestParam(required = false) String keywords);
}
