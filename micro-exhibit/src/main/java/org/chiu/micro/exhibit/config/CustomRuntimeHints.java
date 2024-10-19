package org.chiu.micro.exhibit.config;

import org.chiu.micro.common.dto.BlogSensitiveContentRpcDto;
import org.chiu.micro.common.exception.MissException;
import org.chiu.micro.exhibit.cache.cache.local.CacheBlogEvictMessageListener;
import org.chiu.micro.exhibit.dto.BlogDescriptionDto;
import org.chiu.micro.exhibit.dto.BlogExhibitDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

import java.util.LinkedHashSet;
import java.util.Set;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    private static final Logger log = LoggerFactory.getLogger(CustomRuntimeHints.class);

    @SuppressWarnings("all")
    @Override// Register method for reflection
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // Register method for reflection
        hints.reflection().registerMethod(ReflectionUtils.findMethod(CacheBlogEvictMessageListener.class, "handleMessage", Set.class), ExecutableMode.INVOKE);

        try {
            hints.reflection().registerConstructor(LinkedHashSet.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
        } catch (NoSuchMethodException e) {
            log.error("application start fail");
            throw new MissException(e.getMessage());
        }

        hints.serialization().registerType(BlogExhibitDto.class);
        hints.serialization().registerType(BlogDescriptionDto.class);
        hints.serialization().registerType(BlogSensitiveContentRpcDto.class);

        hints.reflection().registerType(
                TypeReference.of("com.github.benmanes.caffeine.cache.SSMSA"),
                MemberCategory.PUBLIC_FIELDS, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);

        // Register resources
        hints.resources().registerPattern("script/count-years.lua");
        hints.resources().registerPattern("script/visit.lua");
    }
}
