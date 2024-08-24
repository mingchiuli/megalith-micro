package org.chiu.micro.gateway.server.wrapper;

import org.chiu.micro.gateway.server.ExhibitServer;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/public/blog")
public class ExhibitServerWrapper {

    private final ExhibitServer exhibitServer;

    @GetMapping("/info/{id}")
    public byte[] getBlogDetail(@PathVariable Long id, HttpServletRequest request) {
        return exhibitServer.getBlogDetail(id, request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @GetMapping("/page/{currentPage}")
    public byte[] getPage(@PathVariable Integer currentPage,
                                                          @RequestParam(required = false) Integer year) {
        return exhibitServer.findPage(currentPage, year);
    }

    @GetMapping("/secret/{blogId}")
    public byte[] getLockedBlog(@PathVariable Long blogId,
                                               @RequestParam(value = "readToken") String token) {
        return exhibitServer.getLockedBlog(blogId, token);
    }

    @GetMapping("/token/{blogId}")
    public byte[] checkReadToken(@PathVariable Long blogId,
                                          @RequestParam(value = "readToken") String token) {
        return exhibitServer.checkToken(blogId, token);
    }

    @GetMapping("/status/{blogId}")
    public byte[] getBlogStatus(@PathVariable Long blogId,
                                         HttpServletRequest request) {
        return exhibitServer.getBlogStatus(blogId, request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @GetMapping("/years")
    public byte[] searchYears() {
        return exhibitServer.searchYears();
    }

    @GetMapping("/stat")
    public byte[] getVisitStatistics() {
        return exhibitServer.getVisitStatistics();
    }

    @GetMapping("/scores")
    public byte[] getScoreBlogs() {
        return exhibitServer.getScoreBlogs();
    }
}
