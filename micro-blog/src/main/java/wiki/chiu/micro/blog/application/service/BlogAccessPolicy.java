package wiki.chiu.micro.blog.application.service;

import static wiki.chiu.micro.common.lang.ExceptionMessage.EDIT_NO_AUTH;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.domain.BlogEntity;
import wiki.chiu.micro.blog.vo.BlogPermissionsVo;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.BlogStatusEnum;
import wiki.chiu.micro.common.lang.DataPermissionEnum;

@Component
public class BlogAccessPolicy {

    public boolean canCollaborate(
        BlogEntity blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        if (!isAuthenticated(userId)) {
            return false;
        }
        return isOpenForCollaboration(blog) || canEdit(blog, userId, dataPermissions);
    }

    public boolean canEdit(BlogEntity blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        return isAuthenticated(userId)
            && (Objects.equals(blog.getUserId(), userId)
            || has(dataPermissions, DataPermissionEnum.BLOG_EDIT_ALL));
    }

    public boolean canDelete(BlogEntity blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        return isAuthenticated(userId)
            && (Objects.equals(blog.getUserId(), userId)
            || has(dataPermissions, DataPermissionEnum.BLOG_DELETE_ALL));
    }

    public BlogPermissionsVo permissions(
        BlogEntity blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        boolean manage = canEdit(blog, userId, dataPermissions);
        return new BlogPermissionsVo(
            canCollaborate(blog, userId, dataPermissions), manage, manage, manage);
    }

    public void requireCollaboration(
        BlogEntity blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        if (!canCollaborate(blog, userId, dataPermissions)) {
            throw new MissException(EDIT_NO_AUTH.getMsg());
        }
    }

    public void requireEdit(BlogEntity blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        if (!canEdit(blog, userId, dataPermissions)) {
            throw new MissException(EDIT_NO_AUTH.getMsg());
        }
    }

    public void requireAuthenticated(Long userId) {
        if (!isAuthenticated(userId)) {
            throw new MissException(EDIT_NO_AUTH.getMsg());
        }
    }

    private boolean isOpenForCollaboration(BlogEntity blog) {
        return Objects.equals(BlogStatusEnum.NORMAL.getCode(), blog.getStatus())
            || Objects.equals(BlogStatusEnum.DRAFT.getCode(), blog.getStatus());
    }

    private boolean isAuthenticated(Long userId) {
        return userId != null && userId > 0;
    }

    private boolean has(List<DataPermissionEnum> dataPermissions, DataPermissionEnum permission) {
        return dataPermissions != null && dataPermissions.contains(permission);
    }
}
