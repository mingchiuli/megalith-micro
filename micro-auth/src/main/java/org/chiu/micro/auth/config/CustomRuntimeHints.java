package org.chiu.micro.auth.config;

import lombok.SneakyThrows;

import org.chiu.micro.auth.cache.mq.CacheEvictMessageListener;
import org.chiu.micro.auth.dto.ButtonDto;
import org.chiu.micro.auth.dto.MenuWithChildDto;
import org.chiu.micro.auth.dto.MenusAndButtonsDto;
import org.chiu.micro.auth.vo.LoginSuccessVo;
import org.chiu.micro.auth.vo.UserInfoVo;
import org.springframework.aot.hint.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;


import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.util.ReflectionUtils.*;


public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @SuppressWarnings("null")
    @SneakyThrows
    @Override// Register method for reflection
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // Register method for reflection
        hints.reflection().registerMethod(findMethod(CacheEvictMessageListener.class, "handleMessage", Set.class), ExecutableMode.INVOKE);

        hints.reflection().registerConstructor(LinkedHashSet.class.getDeclaredConstructor(), ExecutableMode.INVOKE);

        hints.serialization().registerType(LoginSuccessVo.class);
        hints.serialization().registerType(UserInfoVo.class);
        hints.serialization().registerType(MenusAndButtonsDto.class);
        hints.serialization().registerType(MenuWithChildDto.class);
        hints.serialization().registerType(ButtonDto.class);

        hints.reflection().registerType(
                TypeReference.of("com.github.benmanes.caffeine.cache.SSMSA"),
                MemberCategory.PUBLIC_FIELDS, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);

        // Register resources
        hints.resources().registerPattern("script/email-phone.lua");
        hints.resources().registerPattern("script/password.lua");
        hints.resources().registerPattern("script/statistics.lua");
        hints.resources().registerPattern("script/save-code.lua");
    }
}
