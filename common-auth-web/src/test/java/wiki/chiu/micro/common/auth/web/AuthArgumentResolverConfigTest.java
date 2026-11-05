package wiki.chiu.micro.common.auth.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.annotation.ReflectiveRuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.security.AuthPrincipal;

class AuthArgumentResolverConfigTest {

    @Test
    void configurationAnnotationCoversPrincipalAndNestedPermissions() {
        RuntimeHints hints = new RuntimeHints();
        new ReflectiveRuntimeHintsRegistrar()
            .registerRuntimeHints(hints, AuthArgumentResolverConfig.class);

        assertThat(RuntimeHintsPredicates.reflection()
            .onType(AuthPrincipal.class)
            .withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .test(hints)).isTrue();
        for (var component : AuthPrincipal.class.getRecordComponents()) {
            assertThat(RuntimeHintsPredicates.reflection()
                .onMethodInvocation(component.getAccessor())
                .test(hints)).isTrue();
        }
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(DataPermissionEnum.class, "values")
            .test(hints)).isTrue();
    }
}
