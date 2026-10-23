package wiki.chiu.micro.search.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

import wiki.chiu.micro.search.adapter.out.elasticsearch.AdminBlogDocument;
import wiki.chiu.micro.search.adapter.out.elasticsearch.AdminRevisionDocument;
import wiki.chiu.micro.search.adapter.out.elasticsearch.BlogDocument;

class SearchRuntimeHintsTest {

    @Test
    void registersElasticsearchDocumentsForNativeMapping() {
        RuntimeHints hints = new RuntimeHints();
        new SearchRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertDocumentType(hints, BlogDocument.class);
        assertDocumentType(hints, AdminBlogDocument.class);
        assertDocumentType(hints, AdminRevisionDocument.class);
    }

    private void assertDocumentType(RuntimeHints hints, Class<?> type) {
        assertThat(
                RuntimeHintsPredicates.reflection()
                    .onType(type)
                    .withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                    .test(hints))
            .isTrue();
        assertThat(
                RuntimeHintsPredicates.reflection()
                    .onType(type)
                    .withMemberCategory(MemberCategory.ACCESS_DECLARED_FIELDS)
                    .test(hints))
            .isTrue();
    }
}
