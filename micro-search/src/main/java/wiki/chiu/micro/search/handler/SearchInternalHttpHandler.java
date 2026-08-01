package wiki.chiu.micro.search.handler;


import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;
import wiki.chiu.micro.common.rpc.SearchHttpService;
import wiki.chiu.micro.common.vo.BlogSearchRpcVo;
import wiki.chiu.micro.search.service.BlogSearchService;



@Component
public class SearchInternalHttpHandler implements SearchHttpService {

    private final BlogSearchService blogSearchService;

    public SearchInternalHttpHandler(BlogSearchService blogSearchService) {
        this.blogSearchService = blogSearchService;
    }

    @Override
    public Result<BlogSearchRpcVo> searchBlogs(BlogSysSearchReq req) {
        return Result.success(() -> blogSearchService.searchBlogs(req));
    }

    @Override
    public Result<Long> countBlogs(BlogSysCountSearchReq req) {
        return Result.success(() -> blogSearchService.searchCount(req));
    }

    @Override
    public Result<Void> addReadCount(Long id) {
        return Result.success(() -> blogSearchService.addReadCount(id));
    }
}
