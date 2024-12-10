package wiki.chiu.micro.user.valid;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import wiki.chiu.micro.user.valid.impl.RegisterSaveConstraintValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = {RegisterSaveConstraintValidator.class})
@Target({ PARAMETER })
@Retention(RUNTIME)
public @interface RegisterSave {
    String message() default "{wiki.chiu.micro.user.valid.RegisterSave.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    int[] values() default {};
}
