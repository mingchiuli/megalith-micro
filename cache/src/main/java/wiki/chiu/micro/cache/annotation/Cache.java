package wiki.chiu.micro.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Declares a versioned, read-through two-level cache contract for a Spring-proxied method.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {

    /**
     * Returns the stable, application-owned cache namespace.
     *
     * @return the cache namespace
     */
    String namespace();

    /**
     * Returns the cache contract version.
     *
     * @return the positive contract version
     */
    int version() default 1;

    /**
     * Registers generated keys for application-owned, exact group eviction.
     * Tracked caches bypass storage when the registry is unavailable.
     *
     * @return whether to register keys before loading their values
     */
    boolean trackKeys() default false;

    /**
     * Returns the cache lifetime in {@link #timeUnit()}.
     *
     * @return the positive cache lifetime
     */
    long ttl() default 30;

    /**
     * Returns the unit used by {@link #ttl()}.
     *
     * @return the cache lifetime unit
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;
}
