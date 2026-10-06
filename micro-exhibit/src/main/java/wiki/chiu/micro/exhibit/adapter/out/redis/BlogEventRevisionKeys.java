package wiki.chiu.micro.exhibit.adapter.out.redis;

/**
 * The Redis keys of the per-blog event guard: the lock that serializes the events of one blog and
 * the revision applied to it last. Only this adapter reaches the keys, so they are declared next to
 * the adapter instead of in the core.
 */
final class BlogEventRevisionKeys {

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
