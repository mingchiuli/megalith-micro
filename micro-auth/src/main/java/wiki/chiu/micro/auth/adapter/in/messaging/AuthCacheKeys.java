package wiki.chiu.micro.auth.adapter.in.messaging;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.auth.domain.AuthCacheDescriptors;
import wiki.chiu.micro.cache.key.CacheKeyFactory;
import wiki.chiu.micro.common.message.AuthCacheEvictMessage;

@Component
class AuthCacheKeys {

    private final CacheKeyFactory cacheKeyFactory;

    AuthCacheKeys(CacheKeyFactory cacheKeyFactory) {
        this.cacheKeyFactory = cacheKeyFactory;
    }

    Set<String> from(AuthCacheEvictMessage message) {
        Set<String> keys = new HashSet<>();
        message
            .userIds()
            .forEach(id -> keys.add(cacheKeyFactory.generate(AuthCacheDescriptors.USER_ACCESS, id)));

        if (!message.roleIds().isEmpty()) {
            keys.add(cacheKeyFactory.generate(AuthCacheDescriptors.ROLE_AUTHORIZATION));
        }
        if (message.evictMenus()) {
            message
                .roleCodes()
                .forEach(
                    code ->
                        keys.add(cacheKeyFactory.generate(AuthCacheDescriptors.ROLE_NAVIGATION, code)));
        }
        if (message.evictRoutes()) {
            keys.add(cacheKeyFactory.generate(AuthCacheDescriptors.SYSTEM_AUTHORITIES));
        }
        return keys;
    }
}
