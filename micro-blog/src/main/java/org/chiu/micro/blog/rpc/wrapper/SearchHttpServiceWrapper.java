package org.chiu.micro.blog.rpc.wrapper;


import org.chiu.micro.blog.dto.BlogSearchDto;
import org.chiu.micro.blog.exception.MissException;
import org.chiu.micro.blog.lang.Result;
import org.chiu.micro.blog.rpc.SearchHttpService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SearchHttpServiceWrapper {

    private final SearchHttpService searchHttpService;

    public BlogSearchDto searchBlogs(Integer currentPage, Integer size, String keywords, String token) {
        Result<BlogSearchDto> result = searchHttpService.searchBlogs(currentPage, size, keywords, token);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }
    
}
