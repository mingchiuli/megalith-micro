package wiki.chiu.micro.blog.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import wiki.chiu.micro.blog.dto.BlogDeleteDto;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogSnapshot;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints
            .reflection()
            .registerType(
                BlogDeleteDto.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                BlogChangedMessage.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                BlogSnapshot.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS);

        hints
            .resources()
            .registerPattern("script/hot-blogs.lua")
            .registerPattern("script/blog-delete-list.lua")
            .registerPattern("script/blog-recycle.lua");
    }
}
