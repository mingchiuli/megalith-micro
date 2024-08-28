package org.chiu.micro.user.config;

import lombok.SneakyThrows;

import org.chiu.micro.user.constant.UserAuthMenuOperateMessage;
import org.chiu.micro.user.valid.ListValueConstraintValidator;
import org.chiu.micro.user.valid.MenuValueConstraintValidator;
import org.chiu.micro.user.valid.PhoneConstraintValidator;
import org.chiu.micro.user.valid.UsernameConstraintValidator;
import org.springframework.aot.hint.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.LinkedHashSet;


public class CustomRuntimeHints implements RuntimeHintsRegistrar {
    
    @SneakyThrows
    @Override// Register method for reflection
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // Register method for reflection

        hints.reflection().registerConstructor(LinkedHashSet.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
        hints.reflection().registerConstructor(ListValueConstraintValidator.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
        hints.reflection().registerConstructor(PhoneConstraintValidator.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
        hints.reflection().registerConstructor(UsernameConstraintValidator.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
        hints.reflection().registerConstructor(MenuValueConstraintValidator.class.getDeclaredConstructor(), ExecutableMode.INVOKE);

        hints.serialization().registerType(UserAuthMenuOperateMessage.class);

        hints.resources().registerPattern("ValidationMessages.properties");

    }
}
