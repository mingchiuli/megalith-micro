package wiki.chiu.micro.common.aot.hints;

import java.util.List;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import wiki.chiu.micro.common.lang.BlogOperateMessage;
import wiki.chiu.micro.common.lang.UserAuthMenuOperateMessage;


class CommonRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

        // Hibernate Validator discovers its generated JBoss Logging implementations dynamically.
        hints.reflection().registerTypeIfPresent(classLoader,
                "org.hibernate.validator.internal.util.logging.Log_$logger",
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerTypeIfPresent(classLoader,
                        "org.hibernate.validator.internal.util.logging.Messages_$bundle",
                        MemberCategory.ACCESS_DECLARED_FIELDS);

        hints.reflection().registerType(BlogOperateMessage.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS)
                .registerType(UserAuthMenuOperateMessage.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS);

        // Protobuf discovers the full registry reflectively when OTLP metrics initializes.
        hints.reflection().registerTypeIfPresent(classLoader,
                "com.google.protobuf.ExtensionRegistry",
                typeHint -> typeHint.withMethod("getEmptyRegistry", List.of(), ExecutableMode.INVOKE));

        // Lua scripts for Redis operations
        hints.resources()
                .registerPattern("script/rpush-expire.lua")
                .registerPattern("script/email-phone.lua")
                .registerPattern("script/hot-blogs.lua")
                .registerPattern("script/blog-delete-list.lua")
                .registerPattern("script/password.lua")
                .registerPattern("script/recover-delete.lua")
                .registerPattern("script/hmset-expire.lua")
                .registerPattern("script/multi-pfadd.lua")
                .registerPattern("script/multi-pfcount.lua");
    }
}
