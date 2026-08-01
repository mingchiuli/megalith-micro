package wiki.chiu.micro.blog.handler;

import jakarta.servlet.http.HttpServletResponse;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.service.BlogService;
import wiki.chiu.micro.blog.vo.BlogDeleteVo;
import wiki.chiu.micro.blog.vo.BlogEditVo;
import wiki.chiu.micro.blog.vo.BlogEntityVo;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;

import java.util.List;


/**
 * @author mingchiuli
 * @create 2022-12-01 9:28 pm
 */
@Component
public class BlogHttpHandler {

    private final BlogService blogService;

    public BlogHttpHandler(BlogService blogService) {
        this.blogService = blogService;
    }

    public Result<Void> saveOrUpdate(BlogEntityReq blog, AuthInfo authInfo) {
        return Result.success(() -> blogService.saveOrUpdate(blog, authInfo.userId(), authInfo.roles()));
    }

    public Result<Void> deleteBlogs(List<Long> ids, AuthInfo authInfo) {
        return Result.success(() -> blogService.deleteBatch(ids, authInfo.userId(), authInfo.roles()));
    }

    public Result<String> setBlogToken(Long blogId, AuthInfo authInfo) {
        return Result.success(() -> blogService.setBlogToken(blogId, authInfo.userId()));
    }

    public Result<PageAdapter<BlogEntityVo>> getAllBlogs(BlogQueryReq req, AuthInfo authInfo) {
        return Result.success(() -> blogService.findAllBlogs(req, authInfo.userId(), authInfo.roles()));
    }

    public Result<PageAdapter<BlogDeleteVo>> getDeletedBlogs(Integer currentPage,
                                                             Integer size,
                                                             AuthInfo authInfo) {
        return Result.success(() -> blogService.findDeletedBlogs(currentPage, size, authInfo.userId()));
    }

    public Result<Void> recoverDeletedBlog(Integer idx, AuthInfo authInfo) {
        return Result.success(() -> blogService.recoverDeletedBlog(idx, authInfo.userId()));
    }

    public Result<String> uploadOss(MultipartFile image, AuthInfo authInfo) {
        return Result.success(() -> blogService.uploadOss(image, authInfo.userId()));
    }

    public Result<Void> deleteOss(String url) {
        return Result.success(() -> blogService.deleteOss(url));
    }

    public void download(HttpServletResponse response, BlogDownloadReq req, AuthInfo authInfo) {
        blogService.download(response, req, authInfo.userId(), authInfo.roles());
    }

    public Result<BlogEditVo> getEchoDetail(Long id, AuthInfo authInfo) {
        return Result.success(() -> blogService.findEdit(id, authInfo.userId(), authInfo.roles()));
    }

}
