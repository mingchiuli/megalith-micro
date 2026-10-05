package wiki.chiu.micro.auth.domain;

/**
 * The cache contracts this service owns. The namespaces and versions are domain vocabulary shared by
 * the cached reads and the eviction listener; the adapters turn them into cache keys, so the core
 * stays free of the cache module.
 */
public final class AuthCacheDescriptors {

    public static final int VERSION = 1;
    public static final String USER_ACCESS_NAMESPACE = "auth-user-access";
    public static final String ROLE_AUTHORIZATION_NAMESPACE = "auth-role-authorization";
    public static final String ROLE_NAVIGATION_NAMESPACE = "auth-role-navigation";
    public static final String SYSTEM_AUTHORITIES_NAMESPACE = "auth-system-authorities";

    private AuthCacheDescriptors() {
    }
}
