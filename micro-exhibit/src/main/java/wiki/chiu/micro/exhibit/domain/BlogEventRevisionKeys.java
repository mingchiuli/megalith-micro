package wiki.chiu.micro.exhibit.domain;

/**
 * The keys of the per-blog event guard. The inbound eviction listeners and the Redis guard
 * implementation must agree on them, so they are declared here rather than inside either adapter.
 */
public final class BlogEventRevisionKeys {

    private static final String LOCK_PREFIX = "blog:event-revision:lock:";

    private static final String REVISION_PREFIX = "blog:event-revision:";

    private BlogEventRevisionKeys() {
    }

    public static String lock(Long blogId) {
        return LOCK_PREFIX + blogId;
    }

    public static String revision(Long blogId) {
        return REVISION_PREFIX + blogId;
    }
}
