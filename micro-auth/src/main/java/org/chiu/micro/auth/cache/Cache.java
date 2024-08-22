package org.chiu.micro.auth.cache;

import java.lang.annotation.*;

import org.chiu.micro.auth.lang.Const;

/**
 * @author mingchiuli
 * @create 2021-12-01 7:45 AM
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {

    int expire() default 30;

    Const prefix();

}
