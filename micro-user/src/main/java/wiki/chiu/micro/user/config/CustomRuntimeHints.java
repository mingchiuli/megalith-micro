package wiki.chiu.micro.user.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;
import wiki.chiu.micro.user.valid.impl.PasswordMatchesConstraintValidator;
import wiki.chiu.micro.user.valid.impl.PasswordRequiredForCreateConstraintValidator;


public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection()
                .registerType(PasswordMatchesConstraintValidator.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerType(PasswordRequiredForCreateConstraintValidator.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        // ValidationMessages.properties for Bean Validation
        hints.resources()
                .registerPattern("ValidationMessages.properties");
    }
}
