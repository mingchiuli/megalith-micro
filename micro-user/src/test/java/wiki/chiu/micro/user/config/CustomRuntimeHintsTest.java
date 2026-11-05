package wiki.chiu.micro.user.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.annotation.ReflectiveRuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.web.service.registry.ImportHttpServices;

import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.message.AuthCacheEvictMessage;
import wiki.chiu.micro.common.message.UserDeletedMessage;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.result.Result;
import wiki.chiu.micro.user.MicroUserApplication;
import wiki.chiu.micro.user.adapter.in.http.AuthorityEntityReq;
import wiki.chiu.micro.user.adapter.in.http.AuthorityInternalHttpHandler;
import wiki.chiu.micro.user.adapter.in.http.AuthorityVo;
import wiki.chiu.micro.user.adapter.in.http.MenuAuthorityVo;
import wiki.chiu.micro.user.adapter.in.http.MenuDisplayVo;
import wiki.chiu.micro.user.adapter.in.http.MenuEntityReq;
import wiki.chiu.micro.user.adapter.in.http.MenuEntityVo;
import wiki.chiu.micro.user.adapter.in.http.MenuInternalHttpHandler;
import wiki.chiu.micro.user.adapter.in.http.RegisterImageDeleteReq;
import wiki.chiu.micro.user.adapter.in.http.RoleEntityReq;
import wiki.chiu.micro.user.adapter.in.http.RoleEntityVo;
import wiki.chiu.micro.user.adapter.in.http.RoleMenuVo;
import wiki.chiu.micro.user.adapter.in.http.UserEntityRegisterReq;
import wiki.chiu.micro.user.adapter.in.http.UserEntityReq;
import wiki.chiu.micro.user.adapter.in.http.UserEntityVo;
import wiki.chiu.micro.user.adapter.in.http.UserInternalHttpHandler;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.MenuRpcVo;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.RoleEntityRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

class CustomRuntimeHintsTest {

    @Test
    void httpServiceInterfacesCoverRemovedManualBindings() {
        RuntimeHints hints = new RuntimeHints();
        var registrar = new ReflectiveRuntimeHintsRegistrar();
        for (var group : MicroUserApplication.class.getAnnotationsByType(ImportHttpServices.class)) {
            registrar.registerRuntimeHints(hints, group.types());
        }
        registrar.registerRuntimeHints(
            hints,
            UserInternalHttpHandler.class,
            AuthorityInternalHttpHandler.class,
            MenuInternalHttpHandler.class);

        for (Class<?> type : List.of(
            Result.class,
            UserEntityRpcVo.class,
            UserAccessRpcVo.class,
            RoleAuthorizationRpcVo.class,
            RoleEntityRpcVo.class,
            MenuRpcVo.class,
            AuthorityRpcVo.class,
            DataPermissionEnum.class)) {
            assertBinding(hints, type);
        }
    }

    @Test
    void retainsBindingsForFunctionalRoutesAndManualSerialization() {
        RuntimeHints hints = new RuntimeHints();
        new CustomRuntimeHints().registerHints(hints, getClass().getClassLoader());

        for (Class<?> type : List.of(
            PageAdapter.class,
            UserEntityRegisterReq.class,
            UserEntityReq.class,
            RegisterImageDeleteReq.class,
            RoleEntityReq.class,
            MenuEntityReq.class,
            AuthorityEntityReq.class,
            UserEntityVo.class,
            RoleEntityVo.class,
            RoleMenuVo.class,
            MenuEntityVo.class,
            MenuDisplayVo.class,
            MenuAuthorityVo.class,
            AuthorityVo.class,
            AuthCacheEvictMessage.class,
            UserDeletedMessage.class)) {
            assertBinding(hints, type);
        }
    }

    @Test
    void retainsScriptResources() {
        RuntimeHints hints = new RuntimeHints();
        new CustomRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.resource().forResource("script/consume-registration-token.lua").test(hints))
            .as("Resource hints for consume-registration-token.lua")
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
