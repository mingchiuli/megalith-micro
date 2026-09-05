package wiki.chiu.micro.exhibit.adapter.in.messaging.cache.handler;

import static wiki.chiu.micro.common.lang.Const.*;

import java.util.HashSet;

import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.cache.key.CacheKeyFactory;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.exhibit.adapter.in.messaging.cache.PageCacheEviction;
import wiki.chiu.micro.exhibit.application.port.in.BlogExistenceService;
import wiki.chiu.micro.exhibit.cache.BlogCacheDescriptors;

@Component
public final class DeleteBlogCacheEvictHandler extends BlogCacheEvictHandler {

    private final PageCacheEviction pageCacheEviction;

    private final CacheKeyFactory cacheKeyFactory;
    private final BlogExistenceService blogExistenceService;

    public DeleteBlogCacheEvictHandler(
        RedissonClient redissonClient,
        PageCacheEviction pageCacheEviction,
        CacheEvictor cacheEvictor,
        CacheKeyFactory cacheKeyFactory,
        BlogExistenceService blogExistenceService) {
        super(redissonClient, cacheEvictor);
        this.pageCacheEviction = pageCacheEviction;
        this.cacheKeyFactory = cacheKeyFactory;
        this.blogExistenceService = blogExistenceService;
    }

    @Override
    public boolean supports(BlogOperateEnum blogOperateEnum) {
        return BlogOperateEnum.REMOVE.equals(blogOperateEnum);
    }

    @Override
    public void redisProcess(BlogChangedMessage message) {
        BlogEntityRpcVo blogEntity = blogEntity(message.blogSnapshot());
        Long id = blogEntity.id();

        evictCaches(id);
        pageCacheEviction.evict();
        clearKeys(id);
        blogExistenceService.markAbsent(id);
        deleteHotRead(id);
    }

    private void deleteHotRead(Long id) {
        redissonClient.getScoredSortedSet(HOT_READ).remove(id.toString());
    }

    private void clearKeys(Long id) {
        HashSet<String> clearKeys = new HashSet<>();
        clearKeys.add(READ_TOKEN + id);
        redissonClient.getKeys().delete(clearKeys.toArray(new String[0]));
    }

    private void evictCaches(Long id) {
        HashSet<String> keys = new HashSet<>();

        keys.add(cacheKeyFactory.generate(BlogCacheDescriptors.DETAIL, id));
        keys.add(cacheKeyFactory.generate(BlogCacheDescriptors.SENSITIVE, id));
        cacheEvictor.evict(keys);
    }
}
