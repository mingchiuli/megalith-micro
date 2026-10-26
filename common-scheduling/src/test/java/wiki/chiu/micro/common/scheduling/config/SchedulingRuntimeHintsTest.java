package wiki.chiu.micro.common.scheduling.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeHint;
import org.springframework.aot.hint.TypeReference;

class SchedulingRuntimeHintsTest {

    private static final TypeReference OBJECT = TypeReference.of(Object.class);
    private static final TypeReference RESOURCE_LEAK_TRACKER =
        TypeReference.of("io.netty.util.ResourceLeakTracker");
    private static final TypeReference BYTE_BUF = TypeReference.of("io.netty.buffer.ByteBuf");

    @Test
    void registersNettyLeakDetectorExclusionMethods() {
        RuntimeHints hints = new RuntimeHints();
        new SchedulingRuntimeHints().registerHints(hints, getClass().getClassLoader());

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
