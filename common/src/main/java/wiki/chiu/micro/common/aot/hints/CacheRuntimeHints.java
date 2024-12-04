package wiki.chiu.micro.common.aot.hints;

import org.springframework.aot.hint.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import wiki.chiu.micro.common.req.BlogSysSearchReq;


class CacheRuntimeHints implements RuntimeHintsRegistrar {

    @Override// Register method for reflection
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // Register method for reflection

        hints.serialization().registerType(BlogSysSearchReq.class);

        hints.resources().registerPattern("script/blog-delete.lua");
        hints.resources().registerPattern("script/count-years.lua");
        hints.resources().registerPattern("script/email-phone.lua");
        hints.resources().registerPattern("script/hot-blogs.lua");
        hints.resources().registerPattern("script/list-delete.lua");
        hints.resources().registerPattern("script/password.lua");
        hints.resources().registerPattern("script/push-action.lua");
        hints.resources().registerPattern("script/push-all.lua");
        hints.resources().registerPattern("script/recover-delete.lua");
        hints.resources().registerPattern("script/save-code.lua");
        hints.resources().registerPattern("script/statistics.lua");
        hints.resources().registerPattern("script/visit.lua");
    }

}
