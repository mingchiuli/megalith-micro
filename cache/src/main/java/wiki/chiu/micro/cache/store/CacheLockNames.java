package wiki.chiu.micro.cache.store;

public final class CacheLockNames {

    private static final String REMOTE_LOCK_PREFIX = "megalithRemoteLock:";

    private CacheLockNames() {
    }

    public static String remote(String cacheKey) {
        return REMOTE_LOCK_PREFIX + cacheKey;
    }
}
