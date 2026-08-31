package wiki.chiu.micro.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

import wiki.chiu.micro.auth.dto.MenuDto;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;

class CustomRuntimeHintsTest {

    @Test
    void registersAllCachePayloadTypesForJackson() {
        RuntimeHints hints = new RuntimeHints();
        new CustomRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertJacksonType(hints, MenuDto.class);
        assertJacksonType(hints, UserAccessRpcVo.class);
        assertJacksonType(hints, RoleAuthorizationRpcVo.class);
        assertJacksonType(hints, AuthorityRpcVo.class);
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
