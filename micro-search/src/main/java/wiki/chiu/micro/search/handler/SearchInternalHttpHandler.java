package wiki.chiu.micro.search.handler;


import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;
import wiki.chiu.micro.common.rpc.SearchHttpService;
import wiki.chiu.micro.common.vo.BlogSearchRpcVo;
import wiki.chiu.micro.search.service.BlogSearchService;
import wiki.chiu.micro.common.web.ValidatedRequest;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.positive;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;



@Component
public class SearchInternalHttpHandler implements SearchHttpService {

    private final BlogSearchService blogSearchService;
    private final ValidatedRequest validation;

    public SearchInternalHttpHandler(BlogSearchService blogSearchService, ValidatedRequest validation) {
        this.blogSearchService = blogSearchService;
        this.validation = validation;
    }

    public ServerResponse searchBlogs(ServerRequest request) throws Exception {
        return ok(searchBlogs(validation.body(request, BlogSysSearchReq.class)));
    }

    public ServerResponse countBlogs(ServerRequest request) throws Exception {
        return ok(countBlogs(validation.body(request, BlogSysCountSearchReq.class)));
    }

    public ServerResponse addReadCount(ServerRequest request) {
        Long id = positive(requiredParam(request, "id", Long::valueOf), "id");
        return ok(addReadCount(id));
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
