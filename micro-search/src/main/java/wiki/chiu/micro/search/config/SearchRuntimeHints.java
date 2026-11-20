package wiki.chiu.micro.search.config;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.search.adapter.in.http.BlogDocumentVo;
import wiki.chiu.micro.search.adapter.out.meilisearch.AdminBlogDocument;
import wiki.chiu.micro.search.adapter.out.meilisearch.AdminRevisionDocument;
import wiki.chiu.micro.search.adapter.out.meilisearch.BlogDocument;
import wiki.chiu.micro.search.adapter.out.meilisearch.RecommendationCursor;
import wiki.chiu.micro.search.application.model.PublicBlogRead;

public class SearchRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Payloads and cursor serialization not covered by HTTP service interfaces.
        new BindingReflectionHintsRegistrar()
            .registerReflectionHints(
                hints.reflection(),
                PageAdapter.class,
                BlogDocumentVo.class,
                BlogDocument.class, AdminBlogDocument.class, AdminRevisionDocument.class, PublicBlogRead.class,
                RecommendationCursor.class);
    }
}
