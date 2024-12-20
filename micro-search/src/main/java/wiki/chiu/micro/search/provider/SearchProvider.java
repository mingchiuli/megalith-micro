package wiki.chiu.micro.search.provider;


import org.springframework.web.bind.annotation.*;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;
import wiki.chiu.micro.common.resolver.AuthInfo;
import wiki.chiu.micro.search.service.BlogSearchService;
import wiki.chiu.micro.search.vo.BlogSearchVo;
import org.springframework.validation.annotation.Validated;



@RestController
@Validated
@RequestMapping(value = "/inner")
public class SearchProvider {

    private final BlogSearchService blogSearchService;

    public SearchProvider(BlogSearchService blogSearchService) {
        this.blogSearchService = blogSearchService;
    }

    @PostMapping("/blog/search")
    public Result<BlogSearchVo> searchAllBlogs(@RequestBody BlogSysSearchReq req, AuthInfo authInfo) {
        return Result.success(() -> blogSearchService.searchBlogs(req, authInfo.userId(), authInfo.roles()));
    }

    @PostMapping("/blog/count")
    public Result<Long> searchCount(@RequestBody BlogSysCountSearchReq req, AuthInfo authInfo) {
        return Result.success(() -> blogSearchService.searchCount(req, authInfo.userId(), authInfo.roles()));
    }

    @PostMapping("/blog/read")
    public Result<Void> readCount(@RequestParam Long id) {
        return Result.success(() -> blogSearchService.addReadCount(id));
    }
}
