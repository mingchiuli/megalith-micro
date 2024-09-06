package org.chiu.micro.gateway.server.wrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    public byte[] saveOrUpdate(@RequestBody Object data,
                               HttpServletRequest request) {
        
        return blogServer.saveOrUpdate(data, request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @PostMapping("/delete")
    public byte[] deleteBlogs(@RequestBody List<Long> ids,
                              HttpServletRequest request) {
     
        return blogServer.deleteBatch(ids, request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @GetMapping("/lock/{blogId}")
    public byte[] setBlogToken(@PathVariable Long blogId,
                               HttpServletRequest request) {
        return blogServer.setBlogToken(blogId, request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @GetMapping("/blogs")
    public byte[] getAllBlogs(@RequestParam(required = false) Integer currentPage,
                              @RequestParam(required = false) Integer size,
                              @RequestParam(required = false) String keywords,
                              HttpServletRequest request) {
        return blogServer.findAllABlogs(currentPage, size, keywords, request.getHeader(HttpHeaders.AUTHORIZATION));
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
        Map<String, Object> params = new HashMap<>();
        params.put("fileName", image.getOriginalFilename());
        params.put("data", image.getBytes());
        return blogServer.uploadOss(params, request.getHeader(HttpHeaders.AUTHORIZATION));
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
    public byte[] pullSaveBlog(@RequestBody Object data,
                               HttpServletRequest request) {
        return blogServer.pushAll(data, request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @GetMapping("/edit/pull/echo")
    public byte[] getEchoDetail(@RequestParam(required = false) Long blogId,
                                HttpServletRequest request) {
        return blogServer.findEdit(blogId, request.getHeader(HttpHeaders.AUTHORIZATION));
    }

}
