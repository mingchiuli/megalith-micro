package wiki.chiu.micro.exhibit.application.service;

import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.exhibit.application.port.out.BlogCacheInvalidation;
import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;
import wiki.chiu.micro.exhibit.application.port.out.BlogExistenceStore;

public final class CreateBlogChangeEvictionHandler extends AbstractBlogChangeEvictionHandler {

    private final BlogExistenceStore existence;

    public CreateBlogChangeEvictionHandler(
        BlogEventRevisionGuard revisionGuard,
        BlogCacheInvalidation cacheInvalidation,
        BlogExistenceStore existence) {
        super(revisionGuard, cacheInvalidation);
        this.existence = existence;
    }

    @Override
    public BlogOperateEnum operation() {
        return BlogOperateEnum.CREATE;
    }

    @Override
    protected void applyChangeUnderGuard(BlogChangedMessage message) {
        Long blogId = message.blogSnapshot().id();
        cacheInvalidation.invalidatePages();
        existence.markPresent(blogId);
    }
}
