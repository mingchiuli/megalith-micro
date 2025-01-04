package wiki.chiu.micro.common.aot.hints;

import org.springframework.aot.hint.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;


class CommonRuntimeHints implements RuntimeHintsRegistrar {

    @Override// Register method for reflection
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // Register method for reflection

        hints.resources()
                .registerPattern("script/rpush-expire.lua")
                .registerPattern("script/count-years.lua")
                .registerPattern("script/email-phone.lua")
                .registerPattern("script/hot-blogs.lua")
                .registerPattern("script/blog-list-delete.lua")
                .registerPattern("script/password.lua")
                .registerPattern("script/push-action.lua")
                .registerPattern("script/push-all.lua")
                .registerPattern("script/recover-delete.lua")
                .registerPattern("script/hmset-expire.lua")
                .registerPattern("script/multi-pfadd.lua")
                .registerPattern("script/multi-pfcount.lua");
    }

}
