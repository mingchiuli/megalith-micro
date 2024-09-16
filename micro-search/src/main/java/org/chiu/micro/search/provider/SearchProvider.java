package org.chiu.micro.search.provider;


import org.chiu.micro.search.dto.AuthDto;
import org.chiu.micro.search.lang.Result;
import org.chiu.micro.search.rpc.wrapper.AuthHttpServiceWrapper;
import org.chiu.micro.search.service.BlogSearchService;
import org.chiu.micro.search.vo.BlogSearchVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(value = "/inner")
public class SearchProvider {

    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    private final BlogSearchService blogSearchService;

    @GetMapping("/blog/search")
    public Result<BlogSearchVo> searchAllBlogs(@RequestParam(defaultValue = "1") Integer currentPage,
                                               @RequestParam(defaultValue = "5") Integer size,
                                               @RequestParam @Size(min = 0, max = 20) String keywords) {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogSearchService.searchBlogs(keywords, currentPage, size, authDto.getUserId(), authDto.getRoles()));
    }
}
