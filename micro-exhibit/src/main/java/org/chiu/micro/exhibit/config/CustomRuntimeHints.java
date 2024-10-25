package org.chiu.micro.exhibit.config;

import org.chiu.micro.common.dto.BlogSensitiveContentRpcDto;
import org.chiu.micro.exhibit.dto.BlogDescriptionDto;
import org.chiu.micro.exhibit.dto.BlogExhibitDto;
import org.springframework.aot.hint.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;


public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override// Register method for reflection
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // Register method for reflection

        hints.serialization().registerType(BlogExhibitDto.class);
        hints.serialization().registerType(BlogDescriptionDto.class);
        hints.serialization().registerType(BlogSensitiveContentRpcDto.class);

        // Register resources
        hints.resources().registerPattern("script/count-years.lua");
        hints.resources().registerPattern("script/visit.lua");
    }
}
