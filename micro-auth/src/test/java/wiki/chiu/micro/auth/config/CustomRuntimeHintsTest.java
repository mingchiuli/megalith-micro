package wiki.chiu.micro.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.annotation.ReflectiveRuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.web.service.registry.ImportHttpServices;

import wiki.chiu.micro.auth.MicroAuthApplication;
import wiki.chiu.micro.auth.adapter.in.http.AuthInternalHttpHandler;
import wiki.chiu.micro.auth.adapter.in.http.CodeReq;
import wiki.chiu.micro.auth.adapter.in.http.MenuWithChildVo;
import wiki.chiu.micro.auth.adapter.in.http.UserInfoVo;
import wiki.chiu.micro.auth.adapter.in.security.LoginRequest;
import wiki.chiu.micro.auth.adapter.in.security.LoginType;
import wiki.chiu.micro.auth.api.req.AuthorityRouteReq;
import wiki.chiu.micro.auth.api.req.WebSocketTicketReq;
import wiki.chiu.micro.auth.api.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.auth.application.model.Authority;
import wiki.chiu.micro.auth.application.model.Menu;
import wiki.chiu.micro.auth.application.model.RoleAuthorization;
import wiki.chiu.micro.auth.application.model.UserAccess;
import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.result.Result;
import wiki.chiu.micro.common.security.AuthPrincipal;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;

class CustomRuntimeHintsTest {

    @Test
    void httpServiceInterfacesCoverRemovedManualBindings() {
        RuntimeHints hints = new RuntimeHints();
        var registrar = new ReflectiveRuntimeHintsRegistrar();
        for (var group : MicroAuthApplication.class.getAnnotationsByType(ImportHttpServices.class)) {
            registrar.registerRuntimeHints(hints, group.types());
        }
        registrar.registerRuntimeHints(hints, AuthInternalHttpHandler.class);

        for (Class<?> type : List.of(
            Result.class,
            AuthorityRouteReq.class,
            WebSocketTicketReq.class,
            AuthorityRouteRpcVo.class,
            AuthPrincipal.class,
            DataPermissionEnum.class,
            UserAccessRpcVo.class,
            RoleAuthorizationRpcVo.class,
            AuthorityRpcVo.class)) {
            assertBinding(hints, type);
        }
    }

    @Test
    void retainsBindingsForFunctionalRoutesAndManualSerialization() {
        RuntimeHints hints = new RuntimeHints();
        new CustomRuntimeHints().registerHints(hints, getClass().getClassLoader());

        for (Class<?> type : List.of(
            MenuWithChildVo.class,
            UserInfoVo.class,
            CodeReq.class,
            LoginRequest.class,
            LoginType.class,
            Menu.class,
            UserAccess.class,
            RoleAuthorization.class,
            Authority.class)) {
            assertBinding(hints, type);
        }
    }

    @Test
    void retainsScriptResources() {
        RuntimeHints hints = new RuntimeHints();
        new CustomRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.resource().forResource("script/email-phone.lua").test(hints))
            .as("Resource hints for email-phone.lua")
            .isTrue();
        assertThat(RuntimeHintsPredicates.resource().forResource("script/hmset-expire.lua").test(hints))
            .as("Resource hints for hmset-expire.lua")
            .isTrue();
        assertThat(RuntimeHintsPredicates.resource().forResource("script/multi-pfadd.lua").test(hints))
            .as("Resource hints for multi-pfadd.lua")
            .isTrue();
        assertThat(RuntimeHintsPredicates.resource().forResource("script/password.lua").test(hints))
            .as("Resource hints for password.lua")
            .isTrue();
    }

    private void assertBinding(RuntimeHints hints, Class<?> type) {
        if (type.isEnum()) {
            assertThat(RuntimeHintsPredicates.reflection()
                .onType(type)
                .withMemberCategory(MemberCategory.INVOKE_DECLARED_METHODS)
                .or(RuntimeHintsPredicates.reflection().onMethodInvocation(type, "values"))
                .test(hints))
                .as("Enum binding hints for %s", type.getName())
                .isTrue();
            return;
        }
        assertThat(RuntimeHintsPredicates.reflection()
            .onType(type)
            .withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .test(hints))
            .as("Binding constructor hints for %s", type.getName())
            .isTrue();
        for (var component : type.getRecordComponents()) {
            assertThat(RuntimeHintsPredicates.reflection()
                .onType(type)
                .withMemberCategory(MemberCategory.INVOKE_DECLARED_METHODS)
                .or(RuntimeHintsPredicates.reflection().onMethodInvocation(component.getAccessor()))
                .test(hints))
                .as("Binding accessor hints for %s.%s", type.getName(), component.getName())
                .isTrue();
        }
    }
}
