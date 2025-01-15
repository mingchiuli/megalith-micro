package wiki.chiu.micro.user.valid;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import wiki.chiu.micro.user.valid.impl.MenuValueConstraintValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = {MenuValueConstraintValidator.class})
@Target({ PARAMETER })
@Retention(RUNTIME)
public @interface DeleteMenu {

    String message() default "{wiki.chiu.micro.user.valid.DeleteMenu.message}";


    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
