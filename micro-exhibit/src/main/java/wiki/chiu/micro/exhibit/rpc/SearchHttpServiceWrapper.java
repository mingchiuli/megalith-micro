package wiki.chiu.micro.exhibit.rpc;


import org.springframework.stereotype.Component;
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
        Result.handleResult(() -> searchHttpService.addReadCount(blogId));
    }

}