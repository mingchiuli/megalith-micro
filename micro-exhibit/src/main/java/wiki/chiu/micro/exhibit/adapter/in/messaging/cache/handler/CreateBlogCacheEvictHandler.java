package wiki.chiu.micro.exhibit.adapter.in.messaging.cache.handler;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.exhibit.adapter.in.messaging.cache.PageCacheEviction;
import wiki.chiu.micro.exhibit.application.port.in.BlogExistenceService;
import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;

@Component
public final class CreateBlogCacheEvictHandler extends BlogCacheEvictHandler {

    private final PageCacheEviction pageCacheEviction;
    private final BlogExistenceService blogExistenceService;

    public CreateBlogCacheEvictHandler(
        BlogEventRevisionGuard revisionGuard,
        PageCacheEviction pageCacheEviction,
        CacheEvictor cacheEvictor,
        BlogExistenceService blogExistenceService) {
        super(revisionGuard, cacheEvictor);
        this.pageCacheEviction = pageCacheEviction;
        this.blogExistenceService = blogExistenceService;
    }

    @Override
    public boolean supports(BlogOperateEnum blogOperateEnum) {
        return BlogOperateEnum.CREATE.equals(blogOperateEnum);
    }

    @Override
    protected void applyChange(BlogChangedMessage message) {
        Long blogId = message.blogSnapshot().id();
        pageCacheEviction.evict();
        blogExistenceService.markPresent(blogId);
    }
}
