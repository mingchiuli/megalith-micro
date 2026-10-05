package wiki.chiu.micro.exhibit.adapter.in.messaging.cache.handler;

import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;

public abstract sealed class BlogCacheEvictHandler
    permits CreateBlogCacheEvictHandler, DeleteBlogCacheEvictHandler, UpdateBlogCacheEvictHandler {

    protected final BlogEventRevisionGuard revisionGuard;

    protected final CacheEvictor cacheEvictor;

    protected BlogCacheEvictHandler(BlogEventRevisionGuard revisionGuard, CacheEvictor cacheEvictor) {
        this.revisionGuard = revisionGuard;
        this.cacheEvictor = cacheEvictor;
    }

    public abstract boolean supports(BlogOperateEnum blogOperateEnum);

    protected abstract void applyChange(BlogChangedMessage message);

    public void process(BlogChangedMessage message) {
        Long blogId = message.blogSnapshot().id();
        revisionGuard.applyIfNewer(blogId, message.revision(), () -> applyChange(message));
    }
}
