package wiki.chiu.micro.cache.annotation;

import java.lang.annotation.*;

/**
 * @author mingchiuli
 * @since 2021-12-01 7:45 AM
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {

    int expire() default 30;

    String prefix();

}
