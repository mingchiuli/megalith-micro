package wiki.chiu.micro.blog.valid;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import wiki.chiu.micro.blog.valid.impl.SensitiveContentRangeConstraintValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = SensitiveContentRangeConstraintValidator.class)
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface ValidSensitiveContentRange {

    String message() default "{wiki.chiu.micro.blog.valid.BlogSave.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
