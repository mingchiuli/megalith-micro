package wiki.chiu.micro.exhibit.application.service;

import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.exhibit.application.port.out.BlogCacheInvalidation;
import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;
import wiki.chiu.micro.exhibit.application.port.out.BlogExistenceStore;
import wiki.chiu.micro.exhibit.application.port.out.BlogReadStateStore;

public final class DeleteBlogChangeEvictionHandler extends AbstractBlogChangeEvictionHandler {

    private final BlogReadStateStore readStateStore;

    private final BlogExistenceStore existence;

    public DeleteBlogChangeEvictionHandler(
        BlogEventRevisionGuard revisionGuard,
        BlogCacheInvalidation cacheInvalidation,
        BlogReadStateStore readStateStore,
        BlogExistenceStore existence) {
        super(revisionGuard, cacheInvalidation);
        this.readStateStore = readStateStore;
        this.existence = existence;
    }

    @Override
    public BlogOperateEnum operation() {
        return BlogOperateEnum.REMOVE;
    }

    @Override
    protected void applyChangeUnderGuard(BlogChangedMessage message) {
        Long blogId = message.blogSnapshot().id();
        cacheInvalidation.invalidateBlog(blogId);
        cacheInvalidation.invalidatePages();
        readStateStore.clearReadToken(blogId);
        existence.markAbsent(blogId);
        readStateStore.removeFromHotRead(blogId);
    }
}
