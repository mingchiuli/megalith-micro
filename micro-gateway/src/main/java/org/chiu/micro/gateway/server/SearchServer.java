package org.chiu.micro.gateway.server;


import org.chiu.micro.gateway.lang.Result;
import org.chiu.micro.gateway.page.PageAdapter;
import org.chiu.micro.gateway.vo.BlogDocumentVo;
import org.chiu.micro.gateway.vo.BlogEntityVo;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface SearchServer {

  @GetExchange("/public/blog")
  Result<PageAdapter<BlogDocumentVo>> selectBlogsByES(@RequestParam(required = false) Integer currentPage, @RequestParam Boolean allInfo, @RequestParam(required = false) String year, @RequestParam String keywords);

  @GetExchange("/sys/blogs")
  Result<PageAdapter<BlogEntityVo>> searchAllBlogs(@RequestParam(required = false) Integer currentPage, @RequestParam(required = false) Integer size, @RequestParam String keywords, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);
  
}
