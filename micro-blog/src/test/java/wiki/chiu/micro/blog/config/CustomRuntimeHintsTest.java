package wiki.chiu.micro.blog.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

import wiki.chiu.micro.blog.dto.BlogDeleteDto;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogSnapshot;

class CustomRuntimeHintsTest {

    @Test
    void registersAllManuallySerializedPayloadTypesForJackson() {
        RuntimeHints hints = new RuntimeHints();
        new CustomRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertJacksonType(hints, BlogDeleteDto.class);
        assertJacksonType(hints, BlogChangedMessage.class);
        assertJacksonType(hints, BlogSnapshot.class);
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
