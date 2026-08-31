package wiki.chiu.micro.cache.message;

import java.util.Set;

public record CacheEvictionMessage(int protocolVersion, Set<String> keys) {

    public static final int CURRENT_PROTOCOL_VERSION = 1;

    public CacheEvictionMessage {
        keys = Set.copyOf(keys);
        if (protocolVersion != CURRENT_PROTOCOL_VERSION) {
            throw new IllegalArgumentException(
                "Unsupported cache eviction protocol version: " + protocolVersion);
        }
    }

    public static CacheEvictionMessage of(Set<String> keys) {
        return new CacheEvictionMessage(CURRENT_PROTOCOL_VERSION, keys);
    }
}
