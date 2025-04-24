package wiki.chiu.micro.blog.service;

import jakarta.servlet.http.HttpServletResponse;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.vo.BlogDeleteVo;
import wiki.chiu.micro.blog.vo.BlogEntityVo;
import wiki.chiu.micro.common.page.PageAdapter;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import wiki.chiu.micro.common.vo.BlogEntityRpcVo;

import java.time.LocalDateTime;
import java.util.List;

public interface BlogService {

    void saveOrUpdate(BlogEntityReq blog, Long userId, List<String> roles);

    PageAdapter<BlogEntityVo> findAllBlogs(BlogQueryReq req, Long userId, List<String> roles);

    void recoverDeletedBlog(Integer idx, Long userId);

    PageAdapter<BlogDeleteVo> findDeletedBlogs(Integer currentPage, Integer size, Long userId);

    void deleteBatch(List<Long> ids, Long userId, List<String> roles);

    SseEmitter uploadOss(MultipartFile file, Long userId);

    void deleteOss(String url);

    String setBlogToken(Long blogId, Long userId);

    BlogEntityRpcVo findById(Long blogId);

    List<BlogEntityRpcVo> findAllById(List<Long> ids);

    Long count();

    void setReadCount(Long blogId);

    Integer findStatusById(Long blogId);

    PageAdapter<BlogEntityRpcVo> findPage(Integer pageNo, Integer pageSize);

    Long countByCreatedGreaterThanEqual(LocalDateTime created);

    void download(HttpServletResponse response, BlogDownloadReq req, Long userId, List<String> roles);
}
