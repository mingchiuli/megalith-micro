package org.chiu.micro.gateway.server;

import org.chiu.micro.gateway.req.BlogEditPushAllReq;
import org.chiu.micro.gateway.req.BlogEntityReq;
import org.chiu.micro.gateway.req.DeleteBlogsReq;
import org.chiu.micro.gateway.req.ImgUploadReq;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;


public interface BlogServer {

    @PostExchange("/save")
    byte[] saveOrUpdate(@RequestBody BlogEntityReq blog, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @PostExchange("/delete")
    byte[] deleteBatch(@RequestBody DeleteBlogsReq req, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/lock/{blogId}")
    byte[] setBlogToken(@PathVariable Long blogId, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/blogs")
    byte[] findAllABlogs(@RequestParam(required = false) Integer currentPage, @RequestParam(required = false) Integer size, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/deleted")
    byte[] findDeletedBlogs(@RequestParam Integer currentPage, @RequestParam(value = "size") Integer size, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/recover/{idx}")
    byte[] recoverDeletedBlog(@PathVariable Integer idx, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @PostExchange(value = "/oss/upload")
    byte[] uploadOss(@RequestBody ImgUploadReq image, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/oss/delete")
    byte[] deleteOss(@RequestParam String url);

    @GetExchange("/download")
    byte[] download();

    @PostExchange("/edit/push/all")
    byte[] pushAll(@RequestBody BlogEditPushAllReq blog, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/edit/pull/echo")
    byte[] findEdit(@RequestParam(value = "blogId", required = false) Long id, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);
}
