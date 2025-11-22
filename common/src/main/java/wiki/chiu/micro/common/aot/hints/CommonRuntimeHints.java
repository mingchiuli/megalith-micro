package wiki.chiu.micro.common.aot.hints;

import org.springframework.aot.hint.*;
import wiki.chiu.micro.common.lang.BlogOperateMessage;
import wiki.chiu.micro.common.lang.UserAuthMenuOperateMessage;


class CommonRuntimeHints implements RuntimeHintsRegistrar {

    @Override// Register method for reflection
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register method for reflection


        hints.serialization()
                .registerType(BlogOperateMessage.class)
                .registerType(UserAuthMenuOperateMessage.class);

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
