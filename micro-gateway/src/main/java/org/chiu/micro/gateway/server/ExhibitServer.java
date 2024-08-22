package org.chiu.micro.gateway.server;

import java.util.List;

import org.chiu.micro.gateway.lang.Result;
import org.chiu.micro.gateway.page.PageAdapter;
import org.chiu.micro.gateway.vo.BlogDescriptionVo;
import org.chiu.micro.gateway.vo.BlogExhibitVo;
import org.chiu.micro.gateway.vo.BlogHotReadVo;
import org.chiu.micro.gateway.vo.VisitStatisticsVo;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface ExhibitServer {

    @GetExchange("/info/{blogId}")
    Result<BlogExhibitVo> getBlogDetail(@PathVariable(name = "blogId") Long blogId, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token);

    @GetExchange("/page/{currentPage}")
    Result<PageAdapter<BlogDescriptionVo>> findPage(@PathVariable Integer currentPage, @RequestParam(required = false) Integer year);

    @GetExchange("/secret/{blogId}")
    Result<BlogExhibitVo> getLockedBlog(@PathVariable Long blogId, @RequestParam(value = "readToken") String token);

    @GetExchange("/token/{blogId}")
    Result<Boolean> checkToken(@PathVariable Long blogId, @RequestParam(value = "readToken") String token);

    @GetExchange("/status/{blogId}")
    Result<Integer> getBlogStatus(@PathVariable Long blogId, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token);
    
    @GetExchange("/years")
    Result<List<Integer>> searchYears();

    @GetExchange("/stat")
    Result<VisitStatisticsVo> getVisitStatistics();

    @GetExchange("/scores")
    Result<List<BlogHotReadVo>> getScoreBlogs();
  
}
