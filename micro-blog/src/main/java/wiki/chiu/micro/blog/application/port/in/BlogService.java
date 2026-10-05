package wiki.chiu.micro.blog.application.port.in;

import java.util.List;

import wiki.chiu.micro.blog.application.model.BlogDraft;
import wiki.chiu.micro.blog.application.model.BlogEdit;
import wiki.chiu.micro.blog.application.model.BlogListItem;
import wiki.chiu.micro.blog.application.model.BlogQuery;
import wiki.chiu.micro.blog.application.model.DeletedBlogItem;
import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.page.PageAdapter;

public interface BlogService {

    void saveOrUpdate(BlogDraft blog, Long userId, List<DataPermissionEnum> dataPermissions);

    PageAdapter<BlogListItem> findAllBlogs(
        BlogQuery query, Long userId, List<DataPermissionEnum> dataPermissions);

    void recoverDeletedBlog(Integer idx, Long userId);

    PageAdapter<DeletedBlogItem> findDeletedBlogs(Integer currentPage, Integer size, Long userId);

    void deleteBatch(List<Long> ids, Long userId, List<DataPermissionEnum> dataPermissions);

    void deleteByUserIds(List<Long> userIds);

    BlogEdit findEdit(Long id, Long userId, List<DataPermissionEnum> dataPermissions);
}
