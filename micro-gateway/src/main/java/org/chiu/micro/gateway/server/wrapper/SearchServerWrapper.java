package org.chiu.micro.gateway.server.wrapper;

import org.chiu.micro.gateway.server.SearchServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/search")
public class SearchServerWrapper {

    private final SearchServer searchServer;

    @GetMapping("/public/blog")
    public byte[] selectBlogsByES(@RequestParam(required = false) Integer currentPage,
                                  @RequestParam Boolean allInfo,
                                  @RequestParam(required = false) String year,
                                  @RequestParam String keywords) {
        return searchServer.selectBlogsByES(currentPage, allInfo, year, keywords);
    }
}
