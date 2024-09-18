package org.chiu.micro.exhibit.config;

import lombok.SneakyThrows;
import org.chiu.micro.exhibit.cache.mq.CacheBlogEvictMessageListener;
import org.chiu.micro.exhibit.dto.BlogDescriptionDto;
import org.chiu.micro.exhibit.dto.BlogExhibitDto;
import org.chiu.micro.exhibit.dto.BlogSensitiveContentDto;
import org.springframework.aot.hint.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.util.ReflectionUtils;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {
    
    @SuppressWarnings("all")
    @SneakyThrows
    @Override// Register method for reflection
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // Register method for reflection
        hints.reflection().registerMethod(ReflectionUtils.findMethod(CacheBlogEvictMessageListener.class, "handleMessage", Set.class), ExecutableMode.INVOKE);

        hints.reflection().registerConstructor(LinkedHashSet.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
    
        hints.serialization().registerType(BlogExhibitDto.class);
        hints.serialization().registerType(BlogDescriptionDto.class);
        hints.serialization().registerType(BlogSensitiveContentDto.class);

        hints.reflection().registerType(
                TypeReference.of("com.github.benmanes.caffeine.cache.SSMSA"),
                MemberCategory.PUBLIC_FIELDS, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);

        // Register resources
        hints.resources().registerPattern("script/count-years.lua");
        hints.resources().registerPattern("script/visit.lua");
    }
}
