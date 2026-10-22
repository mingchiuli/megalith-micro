package wiki.chiu.micro.blog.application.port.in;

import java.util.List;

import wiki.chiu.micro.blog.application.model.BlogDraft;
import wiki.chiu.micro.blog.application.model.BlogEdit;
import wiki.chiu.micro.blog.application.model.BlogListItem;
import wiki.chiu.micro.blog.application.model.BlogQuery;
import wiki.chiu.micro.blog.application.model.BlogVersionComparison;
import wiki.chiu.micro.blog.application.model.BlogVersionSummary;
import wiki.chiu.micro.blog.application.model.DeletedBlogItem;
import wiki.chiu.micro.blog.application.model.Page;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;
import wiki.chiu.micro.common.enums.DataPermissionEnum;

public interface BlogService {

    BlogIdentity createDraftBlog(Long userId);

    BlogWorkingDraft saveDraft(
        Long blogId, BlogDraft blog, Long userId, List<DataPermissionEnum> dataPermissions);

    void deleteDraft(Long blogId, Long userId, List<DataPermissionEnum> dataPermissions);

    BlogVersion saveDraftVersion(
        Long blogId, BlogDraft blog, Long userId, List<DataPermissionEnum> dataPermissions);

    void publish(
        Long blogId,
        Long revisionId,
        Integer accessMode,
        Long userId,
        List<DataPermissionEnum> dataPermissions);

    void offline(Long blogId, Long userId, List<DataPermissionEnum> dataPermissions);

    void online(Long blogId, Long userId, List<DataPermissionEnum> dataPermissions);

    BlogVersion findVersion(Long blogId, Long revisionId, Long userId,
        List<DataPermissionEnum> dataPermissions);

    BlogVersionComparison compareVersion(Long blogId, Long revisionId, Long userId,
        List<DataPermissionEnum> dataPermissions);

    Page<BlogVersionSummary> findVersions(
        Long blogId,
        Integer page,
        Integer pageSize,
        String keywords,
        Long userId,
        List<DataPermissionEnum> dataPermissions);

    Page<BlogListItem> findAllBlogs(
        BlogQuery query, Long userId, List<DataPermissionEnum> dataPermissions);

    void recoverDeletedBlog(Integer idx, Long userId);

    Page<DeletedBlogItem> findDeletedBlogs(Integer currentPage, Integer size, Long userId);

    void deleteBatch(List<Long> ids, Long userId, List<DataPermissionEnum> dataPermissions);

    void deleteByUserIds(List<Long> userIds);

    BlogEdit findEdit(Long id, Long userId, List<DataPermissionEnum> dataPermissions);
}
