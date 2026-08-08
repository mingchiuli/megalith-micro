package wiki.chiu.micro.common.aot.hints;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public final class WebRuntimeHints implements RuntimeHintsRegistrar {

  @Override
  public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
    hints
        .reflection()
        .registerTypeIfPresent(
            classLoader,
            "org.hibernate.validator.internal.util.logging.Log_$logger",
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
        .registerTypeIfPresent(
            classLoader,
            "org.hibernate.validator.internal.util.logging.Messages_$bundle",
            MemberCategory.ACCESS_DECLARED_FIELDS);
  }
}
