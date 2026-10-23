package wiki.chiu.micro.user.config;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.message.AuthCacheEvictMessage;
import wiki.chiu.micro.common.message.UserDeletedMessage;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.result.Result;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.MenuRpcVo;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.RoleEntityRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;
import wiki.chiu.micro.user.adapter.in.http.AuthorityEntityReq;
import wiki.chiu.micro.user.adapter.in.http.MenuEntityReq;
import wiki.chiu.micro.user.adapter.in.http.RegisterImageDeleteReq;
import wiki.chiu.micro.user.adapter.in.http.RoleEntityReq;
import wiki.chiu.micro.user.adapter.in.http.UserEntityRegisterReq;
import wiki.chiu.micro.user.adapter.in.http.UserEntityReq;
import wiki.chiu.micro.user.adapter.in.http.AuthorityVo;
import wiki.chiu.micro.user.adapter.in.http.MenuAuthorityVo;
import wiki.chiu.micro.user.adapter.in.http.MenuDisplayVo;
import wiki.chiu.micro.user.adapter.in.http.MenuEntityVo;
import wiki.chiu.micro.user.adapter.in.http.RoleEntityVo;
import wiki.chiu.micro.user.adapter.in.http.RoleMenuVo;
import wiki.chiu.micro.user.adapter.in.http.UserEntityVo;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // HTTP payload types bound by the functional routes in adapter.in.http.
        new BindingReflectionHintsRegistrar()
            .registerReflectionHints(
                hints.reflection(),
                Result.class,
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
                UserEntityRpcVo.class,
                UserAccessRpcVo.class,
                RoleAuthorizationRpcVo.class,
                RoleEntityRpcVo.class,
                MenuRpcVo.class,
                AuthorityRpcVo.class,
                DataPermissionEnum.class);

        hints
            .reflection()
            .registerType(
                AuthCacheEvictMessage.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                UserDeletedMessage.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS);

        hints
            .resources()
            .registerPattern("script/consume-registration-token.lua");
    }
}
