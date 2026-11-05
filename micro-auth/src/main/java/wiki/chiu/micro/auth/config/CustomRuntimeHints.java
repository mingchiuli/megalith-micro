package wiki.chiu.micro.auth.config;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import wiki.chiu.micro.auth.adapter.in.http.CodeReq;
import wiki.chiu.micro.auth.adapter.in.http.MenuWithChildVo;
import wiki.chiu.micro.auth.adapter.in.http.UserInfoVo;
import wiki.chiu.micro.auth.adapter.in.security.LoginRequest;
import wiki.chiu.micro.auth.adapter.in.security.LoginType;
import wiki.chiu.micro.auth.application.model.Authority;
import wiki.chiu.micro.auth.application.model.Menu;
import wiki.chiu.micro.auth.application.model.RoleAuthorization;
import wiki.chiu.micro.auth.application.model.UserAccess;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // HTTP payload types not covered by HTTP service interfaces.
        new BindingReflectionHintsRegistrar()
            .registerReflectionHints(
                hints.reflection(),
                MenuWithChildVo.class,
                UserInfoVo.class,
                CodeReq.class);

        hints
            .reflection()
            .registerType(
                LoginRequest.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(LoginType.class, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                Menu.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                UserAccess.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                RoleAuthorization.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                Authority.class,
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
