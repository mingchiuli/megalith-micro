package wiki.chiu.micro.blog.valid;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import wiki.chiu.micro.blog.valid.impl.PushAllConstraintValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = {PushAllConstraintValidator.class})
@Target({ PARAMETER })
@Retention(RUNTIME)
public @interface PushAllValue {

    String message() default "{wiki.chiu.micro.blog.valid.PushAll.message}";


    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
