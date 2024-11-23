package wiki.chiu.micro.user.valid;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author mingchiuli
 * @create 2023-03-08 1:08 am
 */
@Documented
@Constraint(validatedBy = {UsernameConstraintValidator.class})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface Username {

    String message() default "{wiki.chiu.micro.user.valid.username.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
