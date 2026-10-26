package wiki.chiu.micro.cache.config;

import static org.springframework.util.ReflectionUtils.findMethod;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

import wiki.chiu.micro.cache.adapter.in.messaging.RabbitCacheEvictMessageListener;
import wiki.chiu.micro.cache.application.model.CacheEvictionMessage;

class CacheRuntimeHints implements RuntimeHintsRegistrar {

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

    private static final Logger log = LoggerFactory.getLogger(CacheRuntimeHints.class);

    @SuppressWarnings("all")
    @Override
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        try {
            hints
                .reflection()
                .registerMethod(
                    findMethod(
                        RabbitCacheEvictMessageListener.class,
                        "handleMessage",
                        CacheEvictionMessage.class),
                    ExecutableMode.INVOKE);

            hints
                .reflection()
                .registerType(
                    CacheEvictionMessage.class,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);

            hints
                .reflection()
                .registerType(
                    TypeReference.of(
                        "wiki.chiu.micro.cache.adapter.out.key.JacksonCacheKeyFactory$KeyArgument"),
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS);

            hints
                .reflection()
                .registerType(
                    TypeReference.of("com.github.benmanes.caffeine.cache.PSAMS"),
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

            TypeReference ssmsaType = TypeReference.of("com.github.benmanes.caffeine.cache.SSMSA");
            hints
                .reflection()
                .registerType(
                    ssmsaType,
                    MemberCategory.ACCESS_DECLARED_FIELDS,
                    MemberCategory.ACCESS_PUBLIC_FIELDS,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS)
                .registerType(
                    ssmsaType,
                    typeHint -> {
                        typeHint.withField("FACTORY");
                        typeHint.withField("EXPIRES_AFTER_ACCESS_NANOS");
                    });

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

        } catch (Exception e) {
            log.error("Failed to register runtime hints", e);
            throw new IllegalStateException("Application start failed: " + e.getMessage(), e);
        }
    }
}
