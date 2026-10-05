package wiki.chiu.micro.blog.application.service;

import static wiki.chiu.micro.common.error.ExceptionMessage.EDIT_NO_AUTH;

import java.util.List;
import java.util.Objects;

import wiki.chiu.micro.blog.application.model.BlogPermissions;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.exception.MissException;

public class BlogAccessPolicy {

    public boolean canCollaborate(
        Blog blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        if (!isAuthenticated(userId)) {
            return false;
        }
        return isOpenForCollaboration(blog) || canEdit(blog, userId, dataPermissions);
    }

    public boolean canEdit(Blog blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        return isAuthenticated(userId)
            && (Objects.equals(blog.userId(), userId)
            || has(dataPermissions, DataPermissionEnum.BLOG_EDIT_ALL));
    }

    public boolean canDelete(Blog blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        return isAuthenticated(userId)
            && (Objects.equals(blog.userId(), userId)
            || has(dataPermissions, DataPermissionEnum.BLOG_DELETE_ALL));
    }

    public BlogPermissions permissions(
        Blog blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        boolean manage = canEdit(blog, userId, dataPermissions);
        return new BlogPermissions(
            canCollaborate(blog, userId, dataPermissions), manage, manage, manage);
    }

    public void requireCollaboration(
        Blog blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        if (!canCollaborate(blog, userId, dataPermissions)) {
            throw new MissException(EDIT_NO_AUTH.getMsg());
        }
    }

    public void requireEdit(Blog blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        if (!canEdit(blog, userId, dataPermissions)) {
            throw new MissException(EDIT_NO_AUTH.getMsg());
        }
    }

    public void requireAuthenticated(Long userId) {
        if (!isAuthenticated(userId)) {
            throw new MissException(EDIT_NO_AUTH.getMsg());
        }
    }

    private boolean isOpenForCollaboration(Blog blog) {
        return Objects.equals(BlogStatusEnum.NORMAL.getCode(), blog.status())
            || Objects.equals(BlogStatusEnum.DRAFT.getCode(), blog.status());
    }

    private boolean isAuthenticated(Long userId) {
        return userId != null && userId > 0;
    }

    private boolean has(List<DataPermissionEnum> dataPermissions, DataPermissionEnum permission) {
        return dataPermissions != null && dataPermissions.contains(permission);
    }
}
