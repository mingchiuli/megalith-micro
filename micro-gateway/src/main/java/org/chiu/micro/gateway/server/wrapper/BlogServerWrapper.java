package org.chiu.micro.gateway.server.wrapper;

import java.util.List;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.chiu.micro.gateway.req.BlogEditPushAllReq;
import org.chiu.micro.gateway.req.BlogEntityReq;
import org.chiu.micro.gateway.req.DeleteBlogsReq;
import org.chiu.micro.gateway.req.ImgUploadReq;
import org.chiu.micro.gateway.server.BlogServer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/sys/blog")
public class BlogServerWrapper {

    private final BlogServer blogServer;

    @PostMapping("/save")
    public byte[] saveOrUpdate(@RequestBody BlogEntityReq blog,
                               HttpServletRequest request) {
        
        return blogServer.saveOrUpdate(blog, request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @PostMapping("/delete")
    public byte[] deleteBlogs(@RequestBody List<Long> ids,
                              HttpServletRequest request) {
        var req = new DeleteBlogsReq();
        req.setIds(ids);
        return blogServer.deleteBatch(req, request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @GetMapping("/lock/{blogId}")
    public byte[] setBlogToken(@PathVariable Long blogId,
                               HttpServletRequest request) {
        return blogServer.setBlogToken(blogId, request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @GetMapping("/blogs")
    public byte[] getAllBlogs(@RequestParam(required = false) Integer currentPage,
                              @RequestParam(required = false) Integer size,
                              HttpServletRequest request) {
        return blogServer.findAllABlogs(currentPage, size, request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @GetMapping("/deleted")
    public byte[] getDeletedBlogs(@RequestParam Integer currentPage,
                                  @RequestParam Integer size,
                                  HttpServletRequest request) {
        return blogServer.findDeletedBlogs(currentPage, size, request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @GetMapping("/recover/{idx}")
    public byte[] recoverDeletedBlog(@PathVariable Integer idx,
                                     HttpServletRequest request) {
        return blogServer.recoverDeletedBlog(idx, request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @PostMapping(value = "/oss/upload")
    @SneakyThrows
    public byte[] uploadOss(@RequestParam MultipartFile image,
                            HttpServletRequest request) {
        ImgUploadReq req = new ImgUploadReq();
        req.setData(image.getBytes());
        req.setFileName(image.getOriginalFilename());
        return blogServer.uploadOss(req, request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @GetMapping("/oss/delete")
    public byte[] deleteOss(@RequestParam String url) {
        return blogServer.deleteOss(url);
    }

    @GetMapping("/download")
    @SneakyThrows
    public void download(HttpServletResponse response) {
        byte[] data = blogServer.download();
        response.setContentLength(data.length);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();
    }

    @PostMapping("/edit/push/all")
    public byte[] pullSaveBlog(@RequestBody BlogEditPushAllReq blog,
                               HttpServletRequest request) {
        return blogServer.pushAll(blog, request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @GetMapping("/edit/pull/echo")
    public byte[] getEchoDetail(@RequestParam(value = "blogId", required = false) Long id,
                                HttpServletRequest request) {
        return blogServer.findEdit(id, request.getHeader(HttpHeaders.AUTHORIZATION));
    }

}
