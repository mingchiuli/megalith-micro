package wiki.chiu.micro.blog.application.service;

import static wiki.chiu.micro.common.error.ExceptionMessage.NO_AUTH;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import wiki.chiu.micro.blog.application.model.UploadObject;
import wiki.chiu.micro.blog.application.port.in.BlogAssetService;
import wiki.chiu.micro.blog.application.port.out.BlogAssetStorage;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.exception.MissException;

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
                            && objectName.startsWith(ownerPrefix(blog.userId()))
                            && (url.equals(blog.link())
                            || objectName.startsWith(blogPrefix(blog.userId(), blogId))));
        if (!ownObject && !managedBlogObject) {
            throw new MissException(NO_AUTH);
        }
        storage.delete(objectName);
    }

    private Long assetOwner(Long blogId, Long userId, List<DataPermissionEnum> dataPermissions) {
        if (blogId == null) {
            return userId;
        }
        Blog blog = blogs.findById(blogId).orElseThrow(() -> new MissException(NO_AUTH));
        if (!canEdit(blog, userId, dataPermissions)) {
            throw new MissException(NO_AUTH);
        }
        return blog.userId();
    }

    private boolean canEdit(Blog blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        return Objects.equals(blog.userId(), userId)
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
