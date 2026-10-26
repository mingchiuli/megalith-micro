package wiki.chiu.micro.cache.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeHint;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

class CacheRuntimeHintsTest {

    private static final TypeReference OBJECT = TypeReference.of(Object.class);
    private static final TypeReference RESOURCE_LEAK_TRACKER =
        TypeReference.of("io.netty.util.ResourceLeakTracker");
    private static final TypeReference BYTE_BUF = TypeReference.of("io.netty.buffer.ByteBuf");

    @Test
    void registersCaffeineImplementationsSelectedByLocalCacheConfiguration() {
        RuntimeHints hints = new RuntimeHints();
        new CacheRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertThat(hasDeclaredConstructors("com.github.benmanes.caffeine.cache.PSAMS", hints)).isTrue();
        assertThat(hasDeclaredConstructors("com.github.benmanes.caffeine.cache.SSMSA", hints)).isTrue();
    }

    @Test
    void registersNettyLeakDetectorExclusionMethods() {
        RuntimeHints hints = new RuntimeHints();
        new CacheRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertMethodHint(
            hints,
            "io.netty.util.ReferenceCountUtil",
            "touch",
            OBJECT);
        assertMethodHint(
            hints,
            "io.netty.buffer.AdvancedLeakAwareByteBuf",
            "touch",
            OBJECT);
        assertMethodHint(
            hints,
            "io.netty.buffer.AdvancedLeakAwareByteBuf",
            "recordLeakNonRefCountingOperation",
            RESOURCE_LEAK_TRACKER);
        assertMethodHint(
            hints,
            "io.netty.buffer.AbstractByteBufAllocator",
            "toLeakAwareBuffer",
            BYTE_BUF);
    }

    private boolean hasDeclaredConstructors(String typeName, RuntimeHints hints) {
        return RuntimeHintsPredicates.reflection()
            .onType(TypeReference.of(typeName))
            .withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .test(hints);
    }

    private void assertMethodHint(
        RuntimeHints hints,
        String typeName,
        String methodName,
        TypeReference... parameterTypes) {

        TypeHint typeHint = hints.reflection().getTypeHint(TypeReference.of(typeName));
        assertThat(typeHint).isNotNull();
        assertThat(
            typeHint.methods().anyMatch(methodHint ->
                methodHint.getName().equals(methodName)
                    && methodHint.getParameterTypes().equals(List.of(parameterTypes))))
            .as("Reflection hint for %s.%s", typeName, methodName)
            .isTrue();
    }
}
