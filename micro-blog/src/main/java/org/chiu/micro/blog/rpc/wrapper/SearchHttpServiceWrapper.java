package org.chiu.micro.blog.rpc.wrapper;


import org.chiu.micro.blog.dto.BlogSearchDto;
import org.chiu.micro.blog.exception.MissException;
import org.chiu.micro.blog.lang.Result;
import org.chiu.micro.blog.rpc.SearchHttpService;
import org.springframework.stereotype.Component;

@Component
public class SearchHttpServiceWrapper {

    private final SearchHttpService searchHttpService;

    public SearchHttpServiceWrapper(SearchHttpService searchHttpService) {
        this.searchHttpService = searchHttpService;
    }

    public BlogSearchDto searchBlogs(Integer currentPage, Integer size, String keywords) {
        Result<BlogSearchDto> result = searchHttpService.searchBlogs(currentPage, size, keywords);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

}
