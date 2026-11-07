package wiki.chiu.micro.exhibit.application.service;

import wiki.chiu.micro.exhibit.application.model.BlogChange;
import wiki.chiu.micro.exhibit.application.port.out.BlogCacheInvalidation;
import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;

/**
 * Shared plumbing for the per-operation eviction strategies: every change runs only when the event
 * survived the per-blog revision guard.
 */
abstract sealed class AbstractBlogChangeEvictionHandler implements BlogChangeEvictionHandler
    permits CreateBlogChangeEvictionHandler,
        DeleteBlogChangeEvictionHandler,
        UpdateBlogChangeEvictionHandler {

    private final BlogEventRevisionGuard revisionGuard;

    protected final BlogCacheInvalidation cacheInvalidation;

    protected AbstractBlogChangeEvictionHandler(
        BlogEventRevisionGuard revisionGuard, BlogCacheInvalidation cacheInvalidation) {
        this.revisionGuard = revisionGuard;
        this.cacheInvalidation = cacheInvalidation;
    }

    @Override
    public final void applyChange(BlogChange change) {
        revisionGuard.applyIfNewer(
            change.blogId(),
            change.revision(),
            () -> applyChangeUnderGuard(change));
    }

    protected abstract void applyChangeUnderGuard(BlogChange change);
}
