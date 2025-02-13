package wiki.chiu.micro.blog.rpc;


import wiki.chiu.micro.common.vo.BlogSearchRpcVo;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;
import wiki.chiu.micro.common.rpc.SearchHttpService;
import org.springframework.stereotype.Component;


@Component
public class SearchHttpServiceWrapper {

    private final SearchHttpService searchHttpService;

    public SearchHttpServiceWrapper(SearchHttpService searchHttpService) {
        this.searchHttpService = searchHttpService;
    }

    public BlogSearchRpcVo searchBlogs(BlogSysSearchReq req) {
        return Result.handleResult(() -> searchHttpService.searchBlogs(req));
    }

    public Long countBlogs(BlogSysCountSearchReq req) {
        return Result.handleResult(() -> searchHttpService.countBlogs(req));
    }

}
