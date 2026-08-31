package wiki.chiu.micro.cache.key;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.key.impl.JacksonCacheKeyFactory;

class JacksonCacheKeyFactoryTest {

    private final JacksonCacheKeyFactory factory =
        new JacksonCacheKeyFactory(JsonMapper.builder().build());
    private final CacheDescriptor descriptor = new CacheDescriptor("auth-user-access", 1);

    @Test
    void generatesStableGoldenKey() {
        assertThat(factory.generate(descriptor, 42L))
            .isEqualTo(
                "auth-user-access:v1:95397f53ae85e4dbf652b5cf378683c5a574cd812c8de89e5c34884e799dbb41");
    }

    @Test
    void includesRuntimeTypeInCanonicalArguments() {
        assertThat(factory.generate(descriptor, 42))
            .isNotEqualTo(factory.generate(descriptor, 42L));
    }

    @Test
    void treatsNullVarargsAsOneNullArgument() {
        Object[] nullVarargs = null;

        assertThat(factory.generate(descriptor, nullVarargs))
            .isEqualTo(factory.generate(descriptor, (Object) null));
    }

    @Test
    void sortsMapEntriesBeforeHashing() {
        Map<String, Integer> first = new LinkedHashMap<>();
        first.put("b", 2);
        first.put("a", 1);
        Map<String, Integer> second = new LinkedHashMap<>();
        second.put("a", 1);
        second.put("b", 2);

        assertThat(factory.generate(descriptor, first))
            .isEqualTo(factory.generate(descriptor, second));
    }

    @Test
    void versionChangesTheReadableKeyPrefix() {
        assertThat(factory.generate(new CacheDescriptor("auth-user-access", 2), 42L))
            .startsWith("auth-user-access:v2:")
            .doesNotStartWith("auth-user-access:v1:");
    }
}
