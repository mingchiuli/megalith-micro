package org.chiu.micro.websocket.config;


import org.chiu.micro.websocket.dto.StompMessageDto;
import org.springframework.aot.hint.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;


public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override// Register method for reflection
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // Register resources
        hints.serialization().registerType(StompMessageDto.class);

        hints.resources().registerPattern("script/push-action.lua");
    }
}
