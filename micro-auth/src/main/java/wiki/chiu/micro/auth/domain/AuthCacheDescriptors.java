package wiki.chiu.micro.auth.domain;

import wiki.chiu.micro.cache.key.CacheDescriptor;

/**
 * The cache contracts this service owns. Descriptors are domain vocabulary: they name and version
 * the business data that is cached, and the cached reads and the eviction listener both share them,
 * so they cannot live in either adapter.
 */
public final class AuthCacheDescriptors {

    public static final int VERSION = 1;
    public static final String USER_ACCESS_NAMESPACE = "auth-user-access";
    public static final String ROLE_AUTHORIZATION_NAMESPACE = "auth-role-authorization";
    public static final String ROLE_NAVIGATION_NAMESPACE = "auth-role-navigation";
    public static final String SYSTEM_AUTHORITIES_NAMESPACE = "auth-system-authorities";

    public static final CacheDescriptor USER_ACCESS =
        new CacheDescriptor(USER_ACCESS_NAMESPACE, VERSION);
    public static final CacheDescriptor ROLE_AUTHORIZATION =
        new CacheDescriptor(ROLE_AUTHORIZATION_NAMESPACE, VERSION);
    public static final CacheDescriptor ROLE_NAVIGATION =
        new CacheDescriptor(ROLE_NAVIGATION_NAMESPACE, VERSION);
    public static final CacheDescriptor SYSTEM_AUTHORITIES =
        new CacheDescriptor(SYSTEM_AUTHORITIES_NAMESPACE, VERSION);

    private AuthCacheDescriptors() {
    }
}
