package wiki.chiu.micro.exhibit.application.service;

import static wiki.chiu.micro.common.enums.BlogStatusEnum.NORMAL;

import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.exhibit.application.model.BlogChange;
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
    protected void applyChangeUnderGuard(BlogChange change) {
        Long blogId = change.blogId();
        cacheInvalidation.invalidateBlog(blogId);
        cacheInvalidation.invalidatePages();
        if (NORMAL.getCode().equals(change.status())) {
            readStateStore.clearReadToken(blogId);
        }
    }
}
