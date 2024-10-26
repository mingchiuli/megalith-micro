package wiki.chiu.micro.user.config;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.user.constant.UserAuthMenuOperateMessage;
import wiki.chiu.micro.user.valid.ListValueConstraintValidator;
import wiki.chiu.micro.user.valid.MenuValueConstraintValidator;
import wiki.chiu.micro.user.valid.PhoneConstraintValidator;
import wiki.chiu.micro.user.valid.UsernameConstraintValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.LinkedHashSet;


public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    private static final Logger log = LoggerFactory.getLogger(CustomRuntimeHints.class);

    @Override// Register method for reflection
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // Register method for reflection

        try {
            hints.reflection().registerConstructor(LinkedHashSet.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
            hints.reflection().registerConstructor(ListValueConstraintValidator.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
            hints.reflection().registerConstructor(PhoneConstraintValidator.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
            hints.reflection().registerConstructor(UsernameConstraintValidator.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
            hints.reflection().registerConstructor(MenuValueConstraintValidator.class.getDeclaredConstructor(), ExecutableMode.INVOKE);
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage());
            throw new MissException(e.getMessage());
        }

        hints.serialization().registerType(UserAuthMenuOperateMessage.class);

        hints.resources().registerPattern("ValidationMessages.properties");

    }
}
