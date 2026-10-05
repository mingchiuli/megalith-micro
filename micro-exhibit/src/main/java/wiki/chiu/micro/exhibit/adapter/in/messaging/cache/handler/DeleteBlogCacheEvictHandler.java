package wiki.chiu.micro.exhibit.adapter.in.messaging.cache.handler;

import java.util.HashSet;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.cache.key.CacheKeyFactory;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.exhibit.adapter.in.messaging.cache.PageCacheEviction;
import wiki.chiu.micro.exhibit.application.port.in.BlogExistenceService;
import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;
import wiki.chiu.micro.exhibit.application.port.out.BlogReadStateStore;
import wiki.chiu.micro.exhibit.domain.BlogCacheDescriptors;

@Component
public final class DeleteBlogCacheEvictHandler extends BlogCacheEvictHandler {

    private final PageCacheEviction pageCacheEviction;

    private final CacheKeyFactory cacheKeyFactory;

    private final BlogExistenceService blogExistenceService;

    private final BlogReadStateStore readStateStore;

    public DeleteBlogCacheEvictHandler(
        BlogEventRevisionGuard revisionGuard,
        PageCacheEviction pageCacheEviction,
        CacheEvictor cacheEvictor,
        CacheKeyFactory cacheKeyFactory,
        BlogExistenceService blogExistenceService,
        BlogReadStateStore readStateStore) {
        super(revisionGuard, cacheEvictor);
        this.pageCacheEviction = pageCacheEviction;
        this.cacheKeyFactory = cacheKeyFactory;
        this.blogExistenceService = blogExistenceService;
        this.readStateStore = readStateStore;
    }

    @Override
    public boolean supports(BlogOperateEnum blogOperateEnum) {
        return BlogOperateEnum.REMOVE.equals(blogOperateEnum);
    }

    @Override
    protected void applyChange(BlogChangedMessage message) {
        Long blogId = message.blogSnapshot().id();

        cacheEvictor.evict(detailCacheKeys(blogId));
        pageCacheEviction.evict();
        readStateStore.clearReadToken(blogId);
        blogExistenceService.markAbsent(blogId);
        readStateStore.removeFromHotRead(blogId);
    }

    private HashSet<String> detailCacheKeys(Long blogId) {
        HashSet<String> keys = new HashSet<>();

        keys.add(cacheKeyFactory.generate(BlogCacheDescriptors.DETAIL, blogId));
        keys.add(cacheKeyFactory.generate(BlogCacheDescriptors.SENSITIVE, blogId));
        return keys;
    }
}
