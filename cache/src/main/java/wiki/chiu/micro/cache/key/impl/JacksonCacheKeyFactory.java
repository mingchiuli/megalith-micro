package wiki.chiu.micro.cache.key.impl;

import static tools.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.key.CacheDescriptor;
import wiki.chiu.micro.cache.key.CacheKeyFactory;

public final class JacksonCacheKeyFactory implements CacheKeyFactory {

    private static final HexFormat HEX = HexFormat.of();
    private final ObjectWriter canonicalWriter;

    public JacksonCacheKeyFactory(JsonMapper jsonMapper) {
        this.canonicalWriter =
            jsonMapper
                .rebuild()
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .build()
                .writer()
                .with(ORDER_MAP_ENTRIES_BY_KEYS);
    }

    @Override
    public String generate(CacheDescriptor descriptor, Object... args) {
        Object[] values = args == null ? new Object[]{null} : args;
        KeyArgument[] keyArguments = new KeyArgument[values.length];
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            keyArguments[i] =
                new KeyArgument(value == null ? "null" : value.getClass().getName(), value);
        }

        byte[] canonicalArgs = canonicalWriter.writeValueAsBytes(keyArguments);
        return descriptor.namespace()
            + ":v"
            + descriptor.version()
            + ":"
            + sha256(canonicalArgs);
    }

    private String sha256(byte[] value) {
        try {
            return HEX.formatHex(MessageDigest.getInstance("SHA-256").digest(value));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    record KeyArgument(String type, Object value) {
    }
}
