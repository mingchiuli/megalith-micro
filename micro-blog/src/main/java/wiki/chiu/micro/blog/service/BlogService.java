package wiki.chiu.micro.blog.service;

import java.time.LocalDateTime;
import java.util.List;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.vo.BlogDeleteVo;
import wiki.chiu.micro.blog.vo.BlogEditVo;
import wiki.chiu.micro.blog.vo.BlogEntityVo;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.vo.BlogEntityRpcVo;

public interface BlogService {

  void saveOrUpdate(BlogEntityReq blog, Long userId, List<String> roles);

  PageAdapter<BlogEntityVo> findAllBlogs(BlogQueryReq req, Long userId, List<String> roles);

  void recoverDeletedBlog(Integer idx, Long userId);

  PageAdapter<BlogDeleteVo> findDeletedBlogs(Integer currentPage, Integer size, Long userId);

  void deleteBatch(List<Long> ids, Long userId, List<String> roles);

  BlogEntityRpcVo findById(Long blogId);

  List<BlogEntityRpcVo> findAllById(List<Long> ids);

  Long count();

  void setReadCount(Long blogId);

  PageAdapter<BlogEntityRpcVo> findPage(Integer pageNo, Integer pageSize);

  Long countByCreatedGreaterThanEqual(LocalDateTime created);

  BlogEditVo findEdit(Long id, Long userId, List<String> roles);
}
