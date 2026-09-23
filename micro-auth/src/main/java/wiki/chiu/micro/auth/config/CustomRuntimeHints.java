package wiki.chiu.micro.auth.config;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import wiki.chiu.micro.auth.api.req.AuthorityRouteReq;
import wiki.chiu.micro.auth.api.req.WebSocketTicketReq;
import wiki.chiu.micro.auth.api.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.auth.dto.CodeReq;
import wiki.chiu.micro.auth.dto.LoginRequest;
import wiki.chiu.micro.auth.dto.LoginType;
import wiki.chiu.micro.auth.dto.MenuDto;
import wiki.chiu.micro.auth.vo.MenuWithChildVo;
import wiki.chiu.micro.auth.vo.UserInfoVo;
import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.result.Result;
import wiki.chiu.micro.common.security.AuthPrincipal;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // HTTP payload types bound by the functional routes in adapter.in.http.
        new BindingReflectionHintsRegistrar()
            .registerReflectionHints(
                hints.reflection(),
                Result.class,
                AuthorityRouteReq.class,
                WebSocketTicketReq.class,
                MenuWithChildVo.class,
                UserInfoVo.class,
                AuthorityRouteRpcVo.class,
                AuthPrincipal.class,
                DataPermissionEnum.class,
                CodeReq.class);

        hints
            .reflection()
            .registerType(
                LoginRequest.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(LoginType.class, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                MenuDto.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                UserAccessRpcVo.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                RoleAuthorizationRpcVo.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                AuthorityRpcVo.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS);

        hints
            .resources()
            .registerPattern("script/email-phone.lua")
            .registerPattern("script/password.lua")
            .registerPattern("script/hmset-expire.lua")
            .registerPattern("script/multi-pfadd.lua");
    }
}
