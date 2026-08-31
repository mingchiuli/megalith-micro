package wiki.chiu.micro.exhibit.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

import wiki.chiu.micro.blog.api.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.blog.api.vo.SensitiveContentRpcVo;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.exhibit.dto.BlogDescriptionDto;
import wiki.chiu.micro.exhibit.dto.BlogExhibitDto;

class CustomRuntimeHintsTest {

    @Test
    void registersAllCachePayloadTypesForJackson() {
        RuntimeHints hints = new RuntimeHints();
        new CustomRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertJacksonType(hints, BlogExhibitDto.class);
        assertJacksonType(hints, BlogDescriptionDto.class);
        assertJacksonType(hints, PageAdapter.class);
        assertJacksonType(hints, BlogSensitiveContentRpcVo.class);
        assertJacksonType(hints, SensitiveContentRpcVo.class);
    }

    private void assertJacksonType(RuntimeHints hints, Class<?> type) {
        assertThat(
                RuntimeHintsPredicates.reflection()
                    .onType(type)
                    .withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                    .and(
                        RuntimeHintsPredicates.reflection()
                            .onType(type)
                            .withMemberCategory(MemberCategory.INVOKE_DECLARED_METHODS))
                    .test(hints))
            .as("Jackson reflection hints for %s", type.getName())
            .isTrue();
    }
}
