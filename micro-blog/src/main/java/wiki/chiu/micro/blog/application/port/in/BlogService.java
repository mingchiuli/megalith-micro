package wiki.chiu.micro.blog.application.port.in;

import java.util.List;

import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.vo.BlogDeleteVo;
import wiki.chiu.micro.blog.vo.BlogEditVo;
import wiki.chiu.micro.blog.vo.BlogEntityVo;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.common.page.PageAdapter;

public interface BlogService {

    void saveOrUpdate(BlogEntityReq blog, Long userId, List<DataPermissionEnum> dataPermissions);

    PageAdapter<BlogEntityVo> findAllBlogs(
        BlogQueryReq req, Long userId, List<DataPermissionEnum> dataPermissions);

    void recoverDeletedBlog(Integer idx, Long userId);

    PageAdapter<BlogDeleteVo> findDeletedBlogs(Integer currentPage, Integer size, Long userId);

    void deleteBatch(List<Long> ids, Long userId, List<DataPermissionEnum> dataPermissions);

    void deleteByUserIds(List<Long> userIds);

    BlogEditVo findEdit(Long id, Long userId, List<DataPermissionEnum> dataPermissions);
}
