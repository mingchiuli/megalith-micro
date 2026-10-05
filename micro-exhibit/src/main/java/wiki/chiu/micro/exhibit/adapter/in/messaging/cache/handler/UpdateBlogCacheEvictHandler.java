package wiki.chiu.micro.exhibit.adapter.in.messaging.cache.handler;

import static wiki.chiu.micro.common.enums.BlogStatusEnum.NORMAL;

import java.util.HashSet;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.cache.key.CacheKeyFactory;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.exhibit.adapter.in.messaging.cache.PageCacheEviction;
import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;
import wiki.chiu.micro.exhibit.application.port.out.BlogReadStateStore;
import wiki.chiu.micro.exhibit.domain.BlogCacheDescriptors;

@Component
public final class UpdateBlogCacheEvictHandler extends BlogCacheEvictHandler {

    private final PageCacheEviction pageCacheEviction;

    private final CacheKeyFactory cacheKeyFactory;

    private final BlogReadStateStore readStateStore;

    public UpdateBlogCacheEvictHandler(
        BlogEventRevisionGuard revisionGuard,
        PageCacheEviction pageCacheEviction,
        CacheEvictor cacheEvictor,
        CacheKeyFactory cacheKeyFactory,
        BlogReadStateStore readStateStore) {
        super(revisionGuard, cacheEvictor);
        this.pageCacheEviction = pageCacheEviction;
        this.cacheKeyFactory = cacheKeyFactory;
        this.readStateStore = readStateStore;
    }

    @Override
    public boolean supports(BlogOperateEnum blogOperateEnum) {
        return BlogOperateEnum.UPDATE.equals(blogOperateEnum);
    }

    @Override
    protected void applyChange(BlogChangedMessage message) {
        Long blogId = message.blogSnapshot().id();
        Integer status = message.blogSnapshot().status();

        cacheEvictor.evict(detailCacheKeys(blogId));
        pageCacheEviction.evict();
        if (NORMAL.getCode().equals(status)) {
            readStateStore.clearReadToken(blogId);
        }
    }

    private HashSet<String> detailCacheKeys(Long blogId) {
        HashSet<String> keys = new HashSet<>();

        keys.add(cacheKeyFactory.generate(BlogCacheDescriptors.DETAIL, blogId));
        keys.add(cacheKeyFactory.generate(BlogCacheDescriptors.SENSITIVE, blogId));
        return keys;
    }
}
