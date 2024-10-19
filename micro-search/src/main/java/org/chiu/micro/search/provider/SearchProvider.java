package org.chiu.micro.search.provider;


import jakarta.validation.constraints.Size;
import org.chiu.micro.common.dto.AuthRpcDto;
import org.chiu.micro.common.lang.Result;
import org.chiu.micro.search.rpc.AuthHttpServiceWrapper;
import org.chiu.micro.search.service.BlogSearchService;
import org.chiu.micro.search.vo.BlogSearchVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
