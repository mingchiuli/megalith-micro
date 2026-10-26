package wiki.chiu.micro.common.scheduling.config;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

class SchedulingRuntimeHints implements RuntimeHintsRegistrar {

    private static final TypeReference REFERENCE_COUNT_UTIL =
        TypeReference.of("io.netty.util.ReferenceCountUtil");
    private static final TypeReference ADVANCED_LEAK_AWARE_BYTE_BUF =
        TypeReference.of("io.netty.buffer.AdvancedLeakAwareByteBuf");
    private static final TypeReference ABSTRACT_BYTE_BUF_ALLOCATOR =
        TypeReference.of("io.netty.buffer.AbstractByteBufAllocator");
    private static final TypeReference RESOURCE_LEAK_TRACKER =
        TypeReference.of("io.netty.util.ResourceLeakTracker");
    private static final TypeReference BYTE_BUF = TypeReference.of("io.netty.buffer.ByteBuf");
    private static final TypeReference OBJECT = TypeReference.of(Object.class);

    @Override
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // GraalVM metadata overrides netty-buffer's registration, but Netty reflects over these
        // methods while initializing its leak detector.
        hints
            .reflection()
            .registerType(
                REFERENCE_COUNT_UTIL,
                typeHint -> typeHint.withMethod(
                    "touch",
                    List.of(OBJECT),
                    ExecutableMode.INVOKE))
            .registerType(
                ADVANCED_LEAK_AWARE_BYTE_BUF,
                typeHint -> typeHint
                    .withMethod(
                        "touch",
                        List.of(OBJECT),
                        ExecutableMode.INVOKE)
                    .withMethod(
                        "recordLeakNonRefCountingOperation",
                        List.of(RESOURCE_LEAK_TRACKER),
                        ExecutableMode.INVOKE))
            .registerType(
                ABSTRACT_BYTE_BUF_ALLOCATOR,
                typeHint -> typeHint.withMethod(
                    "toLeakAwareBuffer",
                    List.of(BYTE_BUF),
                    ExecutableMode.INVOKE));
    }
}
