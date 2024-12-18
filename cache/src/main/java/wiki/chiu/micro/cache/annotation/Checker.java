package wiki.chiu.micro.cache.annotation;


import wiki.chiu.micro.cache.handler.CheckerHandler;

import java.lang.annotation.*;

/**
 * @author mingchiuli
 * @since 2022-06-07 11:00 AM
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Checker {
    Class<? extends CheckerHandler> handler();
}
