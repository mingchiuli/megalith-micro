package wiki.chiu.micro.exhibit.adapter.in.messaging.cache.handler;

import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.exhibit.adapter.in.messaging.cache.PageCacheEviction;
import wiki.chiu.micro.exhibit.application.port.in.BlogExistenceService;

@Component
public final class CreateBlogCacheEvictHandler extends BlogCacheEvictHandler {

    private final PageCacheEviction pageCacheEviction;
    private final BlogExistenceService blogExistenceService;

    public CreateBlogCacheEvictHandler(
        RedissonClient redissonClient,
        PageCacheEviction pageCacheEviction,
        CacheEvictor cacheEvictor,
        BlogExistenceService blogExistenceService) {
        super(redissonClient, cacheEvictor);
        this.pageCacheEviction = pageCacheEviction;
        this.blogExistenceService = blogExistenceService;
    }

    @Override
    public boolean supports(BlogOperateEnum blogOperateEnum) {
        return BlogOperateEnum.CREATE.equals(blogOperateEnum);
    }

    @Override
    public void redisProcess(BlogChangedMessage message) {
        BlogEntityRpcVo blogEntity = blogEntity(message.blogSnapshot());
        Long id = blogEntity.id();
        pageCacheEviction.evict();
        blogExistenceService.markPresent(id);
    }
}
