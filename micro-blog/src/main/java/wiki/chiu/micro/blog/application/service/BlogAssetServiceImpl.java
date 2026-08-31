package wiki.chiu.micro.blog.application.service;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_AUTH;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import wiki.chiu.micro.blog.application.model.UploadObject;
import wiki.chiu.micro.blog.application.port.in.BlogAssetService;
import wiki.chiu.micro.blog.application.port.out.BlogAssetStorage;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.domain.BlogEntity;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.DataPermissionEnum;

@Service
public class BlogAssetServiceImpl implements BlogAssetService {

    private final BlogAssetStorage storage;
    private final BlogQueryStore blogs;

    public BlogAssetServiceImpl(BlogAssetStorage storage, BlogQueryStore blogs) {
        this.storage = storage;
        this.blogs = blogs;
    }

    @Override
    public String upload(
        UploadObject upload, Long blogId, Long userId, List<DataPermissionEnum> dataPermissions) {
        Long ownerId = assetOwner(blogId, userId, dataPermissions);
        String objectName = assetPrefix(ownerId, blogId) + UUID.randomUUID();
        return storage.storeImage(objectName, upload.content());
    }

    @Override
    public void delete(
        String url, Long blogId, Long userId, List<DataPermissionEnum> dataPermissions) {
        String objectName = storage.objectName(url);
        boolean ownObject = objectName.startsWith(ownerPrefix(userId));
        boolean managedBlogObject =
            blogId != null
                && blogs.findById(blogId).stream()
                .anyMatch(
                    blog ->
                        canEdit(blog, userId, dataPermissions)
                            && objectName.startsWith(ownerPrefix(blog.getUserId()))
                            && (url.equals(blog.getLink())
                            || objectName.startsWith(blogPrefix(blog.getUserId(), blogId))));
        if (!ownObject && !managedBlogObject) {
            throw new MissException(NO_AUTH);
        }
        storage.delete(objectName);
    }

    private Long assetOwner(Long blogId, Long userId, List<DataPermissionEnum> dataPermissions) {
        if (blogId == null) {
            return userId;
        }
        BlogEntity blog = blogs.findById(blogId).orElseThrow(() -> new MissException(NO_AUTH));
        if (!canEdit(blog, userId, dataPermissions)) {
            throw new MissException(NO_AUTH);
        }
        return blog.getUserId();
    }

    private boolean canEdit(BlogEntity blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        return java.util.Objects.equals(blog.getUserId(), userId)
            || dataPermissions.contains(DataPermissionEnum.BLOG_EDIT_ALL);
    }

    private static String ownerPrefix(Long userId) {
        return "blog/" + userId + "/";
    }

    private static String blogPrefix(Long userId, Long blogId) {
        return ownerPrefix(userId) + blogId + "/";
    }

    private static String assetPrefix(Long userId, Long blogId) {
        return blogId == null ? ownerPrefix(userId) : blogPrefix(userId, blogId);
    }
}
