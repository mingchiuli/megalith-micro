package wiki.chiu.micro.blog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.vo.BlogPermissionsVo;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.BlogStatusEnum;

import java.util.List;
import java.util.Objects;

import static wiki.chiu.micro.common.lang.ExceptionMessage.EDIT_NO_AUTH;

@Component
public class BlogAccessPolicy {

    private final String administratorRole;

    public BlogAccessPolicy(@Value("${megalith.blog.highest-role}") String administratorRole) {
        this.administratorRole = administratorRole;
    }

    public boolean canCollaborate(BlogEntity blog, Long userId, List<String> roles) {
        if (!isAuthenticated(userId)) {
            return false;
        }
        return isOpenForCollaboration(blog) || canManage(blog, userId, roles);
    }

    public boolean canManage(BlogEntity blog, Long userId, List<String> roles) {
        return isAuthenticated(userId)
                && (Objects.equals(blog.getUserId(), userId)
                || roles != null && roles.contains(administratorRole));
    }

    public BlogPermissionsVo permissions(BlogEntity blog, Long userId, List<String> roles) {
        boolean manage = canManage(blog, userId, roles);
        return new BlogPermissionsVo(canCollaborate(blog, userId, roles), manage, manage, manage);
    }

    public void requireCollaboration(BlogEntity blog, Long userId, List<String> roles) {
        if (!canCollaborate(blog, userId, roles)) {
            throw new MissException(EDIT_NO_AUTH.getMsg());
        }
    }

    public void requireManagement(BlogEntity blog, Long userId, List<String> roles) {
        if (!canManage(blog, userId, roles)) {
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
}
