package wiki.chiu.micro.search.provider;


import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;
import wiki.chiu.micro.common.dto.AuthRpcDto;
import wiki.chiu.micro.common.lang.Result;
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
    public Result<BlogSearchVo> searchAllBlogs(@RequestParam(defaultValue = "1") Integer currentPage,
                                               @RequestParam(defaultValue = "5") Integer size,
                                               @RequestParam @Size(max = 20) String keywords) {
        AuthRpcDto authDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogSearchService.searchBlogs(keywords, currentPage, size, authDto.userId(), authDto.roles()));
    }

    @GetMapping("/blog/count")
    public Result<Long> searchCount(@RequestParam @Size(max = 20) String keywords) {
        AuthRpcDto authDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogSearchService.searchCount(keywords, authDto.userId(), authDto.roles()));
    }

    @PostMapping("/blog/read")
    public Result<Void> readCount(@RequestParam Long id) {
        return Result.success(() -> blogSearchService.addReadCount(id));
    }
}
