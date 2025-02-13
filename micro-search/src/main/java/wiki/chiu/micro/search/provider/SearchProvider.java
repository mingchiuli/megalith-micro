package wiki.chiu.micro.search.provider;


import org.springframework.web.bind.annotation.*;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;
import wiki.chiu.micro.common.rpc.SearchHttpService;
import wiki.chiu.micro.common.vo.BlogSearchRpcVo;
import wiki.chiu.micro.search.service.BlogSearchService;
import org.springframework.validation.annotation.Validated;



@RestController
@Validated
@RequestMapping(value = "/inner")
public class SearchProvider implements SearchHttpService {

    private final BlogSearchService blogSearchService;

    public SearchProvider(BlogSearchService blogSearchService) {
        this.blogSearchService = blogSearchService;
    }

    @PostMapping("/blog/search")
    public Result<BlogSearchRpcVo> searchBlogs(@RequestBody BlogSysSearchReq req) {
        return Result.success(() -> blogSearchService.searchBlogs(req));
    }

    @PostMapping("/blog/count")
    public Result<Long> countBlogs(@RequestBody BlogSysCountSearchReq req) {
        return Result.success(() -> blogSearchService.searchCount(req));
    }

    @PostMapping("/blog/read")
    public Result<Void> addReadCount(@RequestParam Long id) {
        return Result.success(() -> blogSearchService.addReadCount(id));
    }
}
