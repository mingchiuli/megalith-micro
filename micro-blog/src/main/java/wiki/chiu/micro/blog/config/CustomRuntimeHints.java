package wiki.chiu.micro.blog.config;

import wiki.chiu.micro.blog.constant.BlogOperateMessage;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.valid.impl.*;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.LinkedHashSet;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override// Register method for reflection
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // Register method for reflection

        try {
            hints.reflection().registerConstructor(LinkedHashSet.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
            hints.reflection().registerConstructor(ListValueConstraintValidator.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
            hints.reflection().registerConstructor(BlogSaveConstraintValidator.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
            hints.reflection().registerConstructor(PushAllConstraintValidator.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
            hints.reflection().registerConstructor(BlogQueryConstraintValidator.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
            hints.reflection().registerConstructor(BlogDownloadConstraintValidator.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("application start fail");
        }

        hints.serialization().registerType(BlogOperateMessage.class);
        hints.serialization().registerType(BlogQueryReq.class);
        hints.serialization().registerType(BlogDownloadReq.class);
    }
}
