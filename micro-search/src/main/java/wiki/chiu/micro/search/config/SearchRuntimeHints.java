package wiki.chiu.micro.search.config;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;
import wiki.chiu.micro.common.lang.BlogSnapshot;

public class SearchRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.resources().registerPattern("script/*.painless");
        new BindingReflectionHintsRegistrar().registerReflectionHints(
            hints.reflection(), BlogSnapshot.class, BlogIndexSourceStatus.class);
    }
}
