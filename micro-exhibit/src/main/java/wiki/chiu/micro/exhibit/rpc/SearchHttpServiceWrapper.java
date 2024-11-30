package wiki.chiu.micro.exhibit.rpc;


import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Result;

import wiki.chiu.micro.common.rpc.SearchHttpService;


/**
 * BlogHttpServiceWrapper
 */
@Component
public class SearchHttpServiceWrapper {

    private final SearchHttpService searchHttpService;

    public SearchHttpServiceWrapper(SearchHttpService searchHttpService) {
        this.searchHttpService = searchHttpService;
    }

    public void addReadCount(Long blogId) {
        Result<Void> result = searchHttpService.addReadCount(blogId);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
    }

}