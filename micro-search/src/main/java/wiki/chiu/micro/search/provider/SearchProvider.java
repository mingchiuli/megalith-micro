package wiki.chiu.micro.search.provider;


import org.springframework.web.bind.annotation.*;
import wiki.chiu.micro.common.dto.AuthRpcDto;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;
import wiki.chiu.micro.search.rpc.AuthHttpServiceWrapper;
import wiki.chiu.micro.search.service.BlogSearchService;
import wiki.chiu.micro.search.vo.BlogSearchVo;
import org.springframework.validation.annotation.Validated;



@RestController
@Validated
@RequestMapping(value = "/inner")
public class SearchProvider {

    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    private final BlogSearchService blogSearchService;

    public SearchProvider(AuthHttpServiceWrapper authHttpServiceWrapper, BlogSearchService blogSearchService) {
        this.authHttpServiceWrapper = authHttpServiceWrapper;
        this.blogSearchService = blogSearchService;
    }

    @GetMapping("/blog/search")
    public Result<BlogSearchVo> searchAllBlogs(BlogSysSearchReq req) {
        AuthRpcDto authDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogSearchService.searchBlogs(req, authDto.userId(), authDto.roles()));
    }

    @PostMapping("/blog/count")
    public Result<Long> searchCount(@RequestBody BlogSysCountSearchReq req) {
        AuthRpcDto authDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogSearchService.searchCount(req, authDto.userId(), authDto.roles()));
    }

    @PostMapping("/blog/read")
    public Result<Void> readCount(@RequestParam Long id) {
        return Result.success(() -> blogSearchService.addReadCount(id));
    }
}
