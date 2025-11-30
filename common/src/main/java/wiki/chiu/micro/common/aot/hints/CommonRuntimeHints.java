package wiki.chiu.micro.common.aot.hints;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;


class CommonRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
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
