package wiki.chiu.micro.auth.config;

import org.springframework.aot.hint.*;
import wiki.chiu.micro.auth.dto.LoginRequest;
import wiki.chiu.micro.auth.dto.LoginType;
import wiki.chiu.micro.auth.dto.MenuDto;
import wiki.chiu.micro.common.lang.AuthCacheEvictMessage;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {

  @Override // Register method for reflection
  public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
    // Register method for reflection
    hints
        .reflection()
        .registerType(
            LoginRequest.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS)
        .registerType(LoginType.class, MemberCategory.INVOKE_DECLARED_METHODS)
        .registerType(
            AuthorityRpcVo.class,
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
            AuthCacheEvictMessage.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS)
        .registerType(
            MenuDto.class,
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
