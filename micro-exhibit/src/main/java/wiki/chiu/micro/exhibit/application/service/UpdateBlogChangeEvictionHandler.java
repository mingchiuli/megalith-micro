package wiki.chiu.micro.exhibit.application.service;

import static wiki.chiu.micro.common.enums.BlogStatusEnum.NORMAL;

import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.exhibit.application.port.out.BlogCacheInvalidation;
import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;
import wiki.chiu.micro.exhibit.application.port.out.BlogReadStateStore;

public final class UpdateBlogChangeEvictionHandler extends AbstractBlogChangeEvictionHandler {

    private final BlogReadStateStore readStateStore;

    public UpdateBlogChangeEvictionHandler(
        BlogEventRevisionGuard revisionGuard,
        BlogCacheInvalidation cacheInvalidation,
        BlogReadStateStore readStateStore) {
        super(revisionGuard, cacheInvalidation);
        this.readStateStore = readStateStore;
    }

    @Override
    public BlogOperateEnum operation() {
        return BlogOperateEnum.UPDATE;
    }

    @Override
    protected void applyChangeUnderGuard(BlogChangedMessage message) {
        Long blogId = message.blogSnapshot().id();
        cacheInvalidation.invalidateBlog(blogId);
        cacheInvalidation.invalidatePages();
        if (NORMAL.getCode().equals(message.blogSnapshot().status())) {
            readStateStore.clearReadToken(blogId);
        }
    }
}
