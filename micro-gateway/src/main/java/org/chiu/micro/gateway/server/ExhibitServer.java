package org.chiu.micro.gateway.server;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface ExhibitServer {

    @GetExchange("/info/{blogId}")
    byte[] getBlogDetail(@PathVariable Long blogId, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token);

    @GetExchange("/page/{currentPage}")
    byte[] findPage(@PathVariable Integer currentPage, @RequestParam(required = false) Integer year);

    @GetExchange("/secret/{blogId}")
    byte[] getLockedBlog(@PathVariable Long blogId, @RequestParam(value = "readToken") String token);

    @GetExchange("/token/{blogId}")
    byte[] checkToken(@PathVariable Long blogId, @RequestParam(value = "readToken") String token);

    @GetExchange("/status/{blogId}")
    byte[] getBlogStatus(@PathVariable Long blogId, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token);
    
    @GetExchange("/years")
    byte[] searchYears();

    @GetExchange("/stat")
    byte[] getVisitStatistics();

    @GetExchange("/scores")
    byte[] getScoreBlogs();
  
}
