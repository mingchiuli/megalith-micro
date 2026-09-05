package wiki.chiu.micro.exhibit.adapter.in.messaging.cache;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.cache.handler.CacheKeyRegistry;
import wiki.chiu.micro.exhibit.cache.BlogCacheDescriptors;

@Component
public class PageCacheEviction {

    private static final int BATCH_SIZE = 256;
    private final CacheKeyRegistry registry;
    private final CacheEvictor evictor;

    public PageCacheEviction(CacheKeyRegistry registry, CacheEvictor evictor) {
        this.registry = registry;
        this.evictor = evictor;
    }

    public void evict() {
        List<String> keys = registry.registeredKeys(BlogCacheDescriptors.PAGE).stream().sorted().toList();
        for (int offset = 0; offset < keys.size(); offset += BATCH_SIZE) {
            evictor.evict(Set.copyOf(keys.subList(offset, Math.min(offset + BATCH_SIZE, keys.size()))));
        }
    }
}
