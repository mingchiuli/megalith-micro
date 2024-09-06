package org.chiu.micro.blog.rpc.wrapper;


import org.chiu.micro.blog.dto.BlogSearchDto;
import org.chiu.micro.blog.exception.MissException;
import org.chiu.micro.blog.lang.Result;
import org.chiu.micro.blog.rpc.SearchHttpService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SearchHttpServiceWrapper {

    private final SearchHttpService searchHttpService;

    public BlogSearchDto searchBlogs(Integer currentPage, Integer size, String keywords) {
        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        Result<BlogSearchDto> result = searchHttpService.searchBlogs(currentPage, size, keywords, request.getHeader(HttpHeaders.AUTHORIZATION));
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }
    
}
