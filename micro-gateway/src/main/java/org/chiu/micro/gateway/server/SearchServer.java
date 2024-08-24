package org.chiu.micro.gateway.server;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface SearchServer {

  @GetExchange("/public/blog")
  byte[] selectBlogsByES(@RequestParam(required = false) Integer currentPage, @RequestParam Boolean allInfo, @RequestParam(required = false) String year, @RequestParam String keywords);

  @GetExchange("/sys/blogs")
  byte[] searchAllBlogs(@RequestParam(required = false) Integer currentPage, @RequestParam(required = false) Integer size, @RequestParam String keywords, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);
  
}
