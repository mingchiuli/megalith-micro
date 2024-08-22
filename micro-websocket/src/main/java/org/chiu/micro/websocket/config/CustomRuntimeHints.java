package org.chiu.micro.websocket.config;


import org.chiu.micro.websocket.dto.StompMessageDto;
import org.springframework.aot.hint.*;


public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override// Register method for reflection
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register resources
        hints.serialization().registerType(StompMessageDto.class);

        hints.resources().registerPattern("script/push-action.lua");
    }
}
