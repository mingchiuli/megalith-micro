package wiki.chiu.micro.exhibit.domain;

/**
 * The cache contracts this service owns. The namespaces and versions are domain vocabulary shared by
 * the cached reads and the eviction listeners; the adapters turn them into cache keys, so the core
 * stays free of the cache module.
 */
public final class BlogCacheDescriptors {

    public static final int VERSION = 2;
    public static final int PAGE_VERSION = 3;
    public static final String DETAIL_NAMESPACE = "blog-detail";
    public static final String PAGE_NAMESPACE = "blog-page";
    public static final String SENSITIVE_NAMESPACE = "blog-sensitive";

    private BlogCacheDescriptors() {
    }
}
