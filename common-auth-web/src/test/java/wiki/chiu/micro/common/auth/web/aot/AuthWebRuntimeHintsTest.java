package wiki.chiu.micro.common.auth.web.aot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

import wiki.chiu.micro.common.security.AuthPrincipal;

class AuthWebRuntimeHintsTest {

    @Test
    void registersAuthPrincipalForManualJacksonCodec() {
        RuntimeHints hints = new RuntimeHints();
        new AuthWebRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertThat(
                RuntimeHintsPredicates.reflection()
                    .onType(AuthPrincipal.class)
                    .withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                    .and(
                        RuntimeHintsPredicates.reflection()
                            .onType(AuthPrincipal.class)
                            .withMemberCategory(MemberCategory.INVOKE_DECLARED_METHODS))
                    .test(hints))
            .isTrue();
    }
}
