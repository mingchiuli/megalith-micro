package org.chiu.micro.cache.aot.hints;

import org.chiu.micro.cache.listener.CacheEvictMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.util.ReflectionUtils.findMethod;

class CacheRuntimeHints implements RuntimeHintsRegistrar {

    private static final Logger log = LoggerFactory.getLogger(CacheRuntimeHints.class);

    @SuppressWarnings("all")
    @Override// Register method for reflection
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // Register method for reflection
        hints.reflection().registerMethod(findMethod(CacheEvictMessageListener.class, "handleMessage", Set.class), ExecutableMode.INVOKE);

        try {
            hints.reflection().registerConstructor(LinkedHashSet.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
        } catch (NoSuchMethodException e) {
            log.error("application start fail");
            throw new RuntimeException(e.getMessage());
        }

        hints.reflection().registerType(
                TypeReference.of("com.github.benmanes.caffeine.cache.SSMSA"),
                MemberCategory.PUBLIC_FIELDS, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
    }
}
