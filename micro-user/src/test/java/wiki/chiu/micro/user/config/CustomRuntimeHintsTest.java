package wiki.chiu.micro.user.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

import wiki.chiu.micro.common.lang.AuthCacheEvictMessage;
import wiki.chiu.micro.common.lang.UserDeletedMessage;

class CustomRuntimeHintsTest {

    @Test
    void registersAllOutboxPayloadTypesForJackson() {
        RuntimeHints hints = new RuntimeHints();
        new CustomRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertJacksonType(hints, AuthCacheEvictMessage.class);
        assertJacksonType(hints, UserDeletedMessage.class);
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
