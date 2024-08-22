package org.chiu.micro.blog.config;

import lombok.SneakyThrows;
import org.chiu.micro.blog.constant.BlogOperateMessage;
import org.chiu.micro.blog.valid.BlogSaveConstraintValidator;
import org.chiu.micro.blog.valid.ListValueConstraintValidator;
import org.chiu.micro.blog.valid.PushAllConstraintValidator;
import org.springframework.aot.hint.*;

import java.util.LinkedHashSet;


public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @SneakyThrows
    @Override// Register method for reflection
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register method for reflection
    
        hints.reflection().registerConstructor(LinkedHashSet.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
        hints.reflection().registerConstructor(ListValueConstraintValidator.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
        hints.reflection().registerConstructor(BlogSaveConstraintValidator.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
        hints.reflection().registerConstructor(PushAllConstraintValidator.class.getDeclaredConstructor(), ExecutableMode.INVOKE);

        hints.serialization().registerType(BlogOperateMessage.class);

        // Register resources
        hints.resources().registerPattern("ValidationMessages.properties");
        hints.resources().registerPattern("script/blog-delete.lua");
        hints.resources().registerPattern("script/hot-blogs.lua");
        hints.resources().registerPattern("script/list-delete.lua");
        hints.resources().registerPattern("script/recover-delete.lua");
        hints.resources().registerPattern("script/push-all.lua");
    }
}
