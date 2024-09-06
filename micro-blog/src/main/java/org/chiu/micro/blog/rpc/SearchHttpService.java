package org.chiu.micro.blog.rpc;



import org.chiu.micro.blog.dto.BlogSearchDto;
import org.chiu.micro.blog.lang.Result;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface SearchHttpService {

    @GetExchange("/blog/search")
    Result<BlogSearchDto> searchBlogs(@RequestParam(required = false) Integer currentPage, @RequestParam(required = false) Integer size, @RequestParam(required = false) String keywords, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

}
