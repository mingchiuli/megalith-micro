package wiki.chiu.micro.exhibit.consumer.cache.handler;

import wiki.chiu.micro.cache.handler.CacheEvictHandler;
import wiki.chiu.micro.common.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.exhibit.consumer.cache.CacheKeyGenerator;
import wiki.chiu.micro.exhibit.rpc.BlogHttpServiceWrapper;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;

import static wiki.chiu.micro.common.lang.Const.*;

@Component
public final class CreateBlogCacheEvictHandler extends BlogCacheEvictHandler {

    private final CacheKeyGenerator cacheKeyGenerator;

    @Value("${megalith.blog.blog-page-size}")
    private int blogPageSize;

    public CreateBlogCacheEvictHandler(RedissonClient redissonClient,
                                       BlogHttpServiceWrapper blogHttpServiceWrapper,
                                       CacheKeyGenerator cacheKeyGenerator,
                                       CacheEvictHandler cacheEvictHandler) {
        super(redissonClient, blogHttpServiceWrapper, cacheEvictHandler);
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    @Override
    public boolean supports(BlogOperateEnum blogOperateEnum) {
        return BlogOperateEnum.CREATE.equals(blogOperateEnum);
    }


    @Override
    public void redisProcess(BlogEntityRpcVo blogEntity) {
        Long id = blogEntity.id();
        long count = blogHttpServiceWrapper.count();

        evictCaches(count);
        rebuildPageBloom(count);
        setBlogDetailBloom(id);
    }

    private void evictCaches(long count) {
        HashSet<String> keys = cacheKeyGenerator.generateHotBlogsKeys(count);
        cacheEvictHandler.evictCache(keys);
    }

    private void rebuildPageBloom(long count) {
        int totalPage = (int) (count % blogPageSize == 0 ? count / blogPageSize : count / blogPageSize + 1);
        for (int i = 1; i <= totalPage; i++) {
            redissonClient.getBitSet(BLOOM_FILTER_PAGE).set(i, true);
        }
    }

    private void setBlogDetailBloom(Long id) {
        redissonClient.getBitSet(BLOOM_FILTER_BLOG).set(id, true);
    }

}
