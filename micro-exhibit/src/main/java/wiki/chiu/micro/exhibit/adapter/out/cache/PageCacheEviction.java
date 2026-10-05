package wiki.chiu.micro.exhibit.adapter.out.cache;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.cache.handler.CacheKeyRegistry;
import wiki.chiu.micro.cache.key.CacheDescriptor;
import wiki.chiu.micro.exhibit.domain.BlogCacheDescriptors;

@Component
public class PageCacheEviction {

    private static final int BATCH_SIZE = 256;
    private static final CacheDescriptor PAGE =
        new CacheDescriptor(BlogCacheDescriptors.PAGE_NAMESPACE, BlogCacheDescriptors.PAGE_VERSION);

    private final CacheKeyRegistry registry;
    private final CacheEvictor evictor;

    public PageCacheEviction(CacheKeyRegistry registry, CacheEvictor evictor) {
        this.registry = registry;
        this.evictor = evictor;
    }

    public void evict() {
        List<String> keys = registry.registeredKeys(PAGE).stream().sorted().toList();
        for (int offset = 0; offset < keys.size(); offset += BATCH_SIZE) {
            evictor.evict(Set.copyOf(keys.subList(offset, Math.min(offset + BATCH_SIZE, keys.size()))));
        }
    }
}
