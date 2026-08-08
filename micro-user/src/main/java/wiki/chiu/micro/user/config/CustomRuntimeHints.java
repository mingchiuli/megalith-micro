package wiki.chiu.micro.user.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import wiki.chiu.micro.common.lang.UserAuthMenuOperateMessage;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {

  @Override
  public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
    hints
        .reflection()
        .registerType(
            UserAuthMenuOperateMessage.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS);

    // ValidationMessages.properties for Bean Validation
    hints.resources().registerPattern("ValidationMessages.properties");
  }
}
