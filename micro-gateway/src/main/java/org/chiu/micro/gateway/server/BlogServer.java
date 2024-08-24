package org.chiu.micro.gateway.server;

import org.chiu.micro.gateway.lang.Result;
import org.chiu.micro.gateway.page.PageAdapter;
import org.chiu.micro.gateway.req.BlogEditPushAllReq;
import org.chiu.micro.gateway.req.BlogEntityReq;
import org.chiu.micro.gateway.req.DeleteBlogsReq;
import org.chiu.micro.gateway.req.ImgUploadReq;
import org.chiu.micro.gateway.vo.BlogDeleteVo;
import org.chiu.micro.gateway.vo.BlogEditVo;
import org.chiu.micro.gateway.vo.BlogEntityVo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


public interface BlogServer {

    @PostExchange("/save")
    Result<Void> saveOrUpdate(@RequestBody BlogEntityReq blog, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @PostExchange("/delete")
    Result<Void> deleteBatch(@RequestBody DeleteBlogsReq req, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/lock/{blogId}")
    Result<String> setBlogToken(@PathVariable Long blogId, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/blogs")
    Result<PageAdapter<BlogEntityVo>> findAllABlogs(@RequestParam(required = false) Integer currentPage, @RequestParam(required = false) Integer size, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/deleted")
    Result<PageAdapter<BlogDeleteVo>> findDeletedBlogs(@RequestParam Integer currentPage, @RequestParam(value = "size") Integer size, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/recover/{idx}")
    Result<Void> recoverDeletedBlog(@PathVariable Integer idx, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @PostExchange(value = "/oss/upload", contentType = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter uploadOss(@RequestBody ImgUploadReq image, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/oss/delete")
    Result<Void> deleteOss(@RequestParam String url);

    @GetExchange("/download")
    byte[] download();

    @PostExchange("/edit/push/all")
    Result<Void> pushAll(@RequestBody BlogEditPushAllReq blog, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @GetExchange("/edit/pull/echo")
    Result<BlogEditVo> findEdit(@RequestParam(value = "blogId", required = false) Long id, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);
}
