package wiki.chiu.micro.exhibit.domain;

import wiki.chiu.micro.cache.key.CacheDescriptor;

/**
 * The cache contracts this service owns. Descriptors are domain vocabulary: they name and version
 * the business data that is cached, and the cached reads and the eviction listeners both share
 * them, so they cannot live in either adapter.
 */
public final class BlogCacheDescriptors {

    public static final int VERSION = 2;
    public static final int PAGE_VERSION = 3;
    public static final String DETAIL_NAMESPACE = "blog-detail";
    public static final String PAGE_NAMESPACE = "blog-page";
    public static final String SENSITIVE_NAMESPACE = "blog-sensitive";

    public static final CacheDescriptor DETAIL = new CacheDescriptor(DETAIL_NAMESPACE, VERSION);
    public static final CacheDescriptor PAGE = new CacheDescriptor(PAGE_NAMESPACE, PAGE_VERSION);
    public static final CacheDescriptor SENSITIVE =
        new CacheDescriptor(SENSITIVE_NAMESPACE, VERSION);

    private BlogCacheDescriptors() {
    }
}
