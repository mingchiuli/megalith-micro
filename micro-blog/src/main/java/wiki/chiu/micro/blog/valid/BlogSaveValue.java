package wiki.chiu.micro.blog.valid;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import wiki.chiu.micro.blog.valid.impl.BlogSaveConstraintValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = {BlogSaveConstraintValidator.class})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface BlogSaveValue {

    String message() default "{wiki.chiu.micro.blog.valid.BlogSave.message}";


    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
