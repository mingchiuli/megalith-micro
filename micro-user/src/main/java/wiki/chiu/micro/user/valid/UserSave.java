package wiki.chiu.micro.user.valid;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import wiki.chiu.micro.user.valid.impl.UserSaveConstraintValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = {UserSaveConstraintValidator.class})
@Target({ PARAMETER })
@Retention(RUNTIME)
public @interface UserSave {
    String message() default "{wiki.chiu.micro.user.valid.UserSave.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    int[] values() default {};
}
