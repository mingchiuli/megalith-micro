package wiki.chiu.micro.cache.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.cache.annotation.Cache;

class CacheContractValidatorTest {

    @Test
    void rejectsDescriptorReuseAcrossMethods() {
        CacheContractValidator validator = new CacheContractValidator();
        validator.postProcessBeforeInitialization(new FirstCache(), "first");

        assertThatThrownBy(
            () -> validator.postProcessBeforeInitialization(new DuplicateCache(), "duplicate"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cache descriptor CacheDescriptor[namespace=shared, version=1]");
    }

    @Test
    void rejectsNonPositiveTtlAtStartup() {
        CacheContractValidator validator = new CacheContractValidator();

        assertThatThrownBy(
            () -> validator.postProcessBeforeInitialization(new InvalidTtlCache(), "invalid"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cache TTL must be positive");
    }

    static final class FirstCache {

        @Cache(namespace = "shared")
        String load() {
            return "first";
        }
    }

    static final class DuplicateCache {

        @Cache(namespace = "shared")
        String load() {
            return "duplicate";
        }
    }

    static final class InvalidTtlCache {

        @Cache(namespace = "invalid", ttl = 0)
        String load() {
            return "invalid";
        }
    }
}
