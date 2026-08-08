package wiki.chiu.micro.blog.service;

import java.util.List;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.vo.BlogDeleteVo;
import wiki.chiu.micro.blog.vo.BlogEditVo;
import wiki.chiu.micro.blog.vo.BlogEntityVo;
import wiki.chiu.micro.common.page.PageAdapter;

public interface BlogService {

  void saveOrUpdate(BlogEntityReq blog, Long userId, List<String> roles);

  PageAdapter<BlogEntityVo> findAllBlogs(BlogQueryReq req, Long userId, List<String> roles);

  void recoverDeletedBlog(Integer idx, Long userId);

  PageAdapter<BlogDeleteVo> findDeletedBlogs(Integer currentPage, Integer size, Long userId);

  void deleteBatch(List<Long> ids, Long userId, List<String> roles);

  BlogEditVo findEdit(Long id, Long userId, List<String> roles);
}
