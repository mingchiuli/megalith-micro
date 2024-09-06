package org.chiu.micro.gateway.server;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;


public interface BlogServer {

    @PostExchange("/save")
    byte[] saveOrUpdate(@RequestBody Object data, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @PostExchange("/delete")
    byte[] deleteBatch(@RequestBody List<Long> ids, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/lock/{blogId}")
    byte[] setBlogToken(@PathVariable Long blogId, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/blogs")
    byte[] findAllABlogs(@RequestParam(required = false) Integer currentPage, @RequestParam(required = false) Integer size, @RequestParam(required = false) String keywords, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/deleted")
    byte[] findDeletedBlogs(@RequestParam Integer currentPage, @RequestParam Integer size, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/recover/{idx}")
    byte[] recoverDeletedBlog(@PathVariable Integer idx, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @PostExchange(value = "/oss/upload")
    byte[] uploadOss(@RequestBody Map<String, Object> image, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/oss/delete")
    byte[] deleteOss(@RequestParam String url);

    @GetExchange("/download")
    byte[] download();

    @PostExchange("/edit/push/all")
    byte[] pushAll(@RequestBody Object blog, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/edit/pull/echo")
    byte[] findEdit(@RequestParam(value = "blogId", required = false) Long id, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);
}
