package org.chiu.micro.user.valid;

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
 * @create 2023-03-08 1:24 am
 */
@Documented
@Constraint(validatedBy = {PhoneConstraintValidator.class})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface Phone {

    String message() default "{org.chiu.micro.user.phone.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}

