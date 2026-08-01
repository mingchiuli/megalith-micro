package wiki.chiu.micro.user.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;
import wiki.chiu.micro.user.valid.impl.ListValueConstraintValidator;
import wiki.chiu.micro.user.valid.impl.RegisterSaveConstraintValidator;
import wiki.chiu.micro.user.valid.impl.UserSaveConstraintValidator;


public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection()
                .registerType(ListValueConstraintValidator.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerType(RegisterSaveConstraintValidator.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerType(UserSaveConstraintValidator.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        // ValidationMessages.properties for Bean Validation
        hints.resources()
                .registerPattern("ValidationMessages.properties");
    }
}
