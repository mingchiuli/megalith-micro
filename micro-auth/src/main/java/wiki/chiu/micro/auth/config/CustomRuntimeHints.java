package wiki.chiu.micro.auth.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import wiki.chiu.micro.auth.dto.LoginRequest;
import wiki.chiu.micro.auth.dto.LoginType;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {

  @Override
  public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
    hints
        .reflection()
        .registerType(
            LoginRequest.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS)
        .registerType(LoginType.class, MemberCategory.INVOKE_DECLARED_METHODS);

    hints
        .resources()
        .registerPattern("script/email-phone.lua")
        .registerPattern("script/password.lua")
        .registerPattern("script/hmset-expire.lua")
        .registerPattern("script/multi-pfadd.lua");
  }
}
