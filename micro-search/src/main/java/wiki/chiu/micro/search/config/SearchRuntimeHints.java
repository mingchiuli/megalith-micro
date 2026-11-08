package wiki.chiu.micro.search.config;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import wiki.chiu.micro.common.message.BlogAdminIndexMessage;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.search.adapter.in.http.BlogDocumentVo;
import wiki.chiu.micro.search.adapter.out.elasticsearch.RecommendationCursor;

public class SearchRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.resources().registerPattern("script/*.painless");

        // Payloads and cursor serialization not covered by HTTP service interfaces.
        new BindingReflectionHintsRegistrar()
            .registerReflectionHints(
                hints.reflection(),
                PageAdapter.class,
                BlogDocumentVo.class,
                RecommendationCursor.class,
                BlogAdminIndexMessage.class);
    }
}
