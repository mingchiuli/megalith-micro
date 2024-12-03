package wiki.chiu.micro.user.valid;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import wiki.chiu.micro.user.valid.impl.PhoneConstraintValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author mingchiuli
 * @create 2023-03-08 1:24 am
 */
@Documented
@Constraint(validatedBy = {PhoneConstraintValidator.class})
@Target({ PARAMETER })
@Retention(RUNTIME)
public @interface Phone {

    String message() default "{wiki.chiu.micro.user.valid.phone.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}

