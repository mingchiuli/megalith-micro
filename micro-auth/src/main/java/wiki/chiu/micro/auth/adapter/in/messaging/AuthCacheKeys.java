package wiki.chiu.micro.auth.adapter.in.messaging;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.auth.domain.AuthCacheDescriptors;
import wiki.chiu.micro.cache.key.CacheDescriptor;
import wiki.chiu.micro.cache.key.CacheKeyFactory;
import wiki.chiu.micro.common.message.AuthCacheEvictMessage;

@Component
class AuthCacheKeys {

    private static final CacheDescriptor USER_ACCESS =
        new CacheDescriptor(AuthCacheDescriptors.USER_ACCESS_NAMESPACE, AuthCacheDescriptors.VERSION);
    private static final CacheDescriptor ROLE_AUTHORIZATION =
        new CacheDescriptor(
            AuthCacheDescriptors.ROLE_AUTHORIZATION_NAMESPACE, AuthCacheDescriptors.VERSION);
    private static final CacheDescriptor ROLE_NAVIGATION =
        new CacheDescriptor(
            AuthCacheDescriptors.ROLE_NAVIGATION_NAMESPACE, AuthCacheDescriptors.VERSION);
    private static final CacheDescriptor SYSTEM_AUTHORITIES =
        new CacheDescriptor(
            AuthCacheDescriptors.SYSTEM_AUTHORITIES_NAMESPACE, AuthCacheDescriptors.VERSION);

    private final CacheKeyFactory cacheKeyFactory;

    AuthCacheKeys(CacheKeyFactory cacheKeyFactory) {
        this.cacheKeyFactory = cacheKeyFactory;
    }

    Set<String> from(AuthCacheEvictMessage message) {
        Set<String> keys = new HashSet<>();
        message.userIds().forEach(id -> keys.add(cacheKeyFactory.generate(USER_ACCESS, id)));

        if (!message.roleIds().isEmpty()) {
            keys.add(cacheKeyFactory.generate(ROLE_AUTHORIZATION));
        }
        if (message.evictMenus()) {
            message
                .roleCodes()
                .forEach(code -> keys.add(cacheKeyFactory.generate(ROLE_NAVIGATION, code)));
        }
        if (message.evictRoutes()) {
            keys.add(cacheKeyFactory.generate(SYSTEM_AUTHORITIES));
        }
        return keys;
    }
}
