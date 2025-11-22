/**
 * Megalith Cache Spring Boot Starter Module
 *
 * A cache framework that provides both local and remote caching capabilities
 * with cache eviction support through RabbitMQ or Redis pub/sub.
 *
 * @author mingchiuli
 * @since 2025
 */
module wiki.chiu.megalith.cache {
    requires org.slf4j;
    requires spring.core;
    requires org.aspectj.weaver;
    requires redisson;
    requires com.github.benmanes.caffeine;
    requires spring.beans;
    requires spring.context;
    requires spring.boot.autoconfigure;
    requires spring.amqp;
    requires spring.rabbit;
    requires jakarta.annotation;
    requires org.jspecify;
    requires spring.boot.amqp;
    requires tools.jackson.databind;


    // Exports - public API packages
    exports wiki.chiu.micro.cache.annotation;
    exports wiki.chiu.micro.cache.handler;
    exports wiki.chiu.micro.cache.config;

    // Exports for Spring Boot auto-configuration
    exports wiki.chiu.micro.cache.aspect to spring.core, spring.context;
    exports wiki.chiu.micro.cache.utils to spring.core, spring.context;
    exports wiki.chiu.micro.cache.listener to spring.core, spring.context;
    exports wiki.chiu.micro.cache.handler.impl to spring.core, spring.context;
    exports wiki.chiu.micro.cache.aot.hints to spring.core, spring.context;

    // Opens packages for reflection (required for Spring and Jackson)
    opens wiki.chiu.micro.cache.config to spring.core, spring.context;
    opens wiki.chiu.micro.cache.aspect to spring.core, spring.context;
    opens wiki.chiu.micro.cache.listener to spring.core, spring.context, com.fasterxml.jackson.databind;
    opens wiki.chiu.micro.cache.handler.impl to spring.core, spring.context;
    opens wiki.chiu.micro.cache.utils to spring.core, spring.context;
    opens wiki.chiu.micro.cache.aot.hints to spring.core, spring.context;
}

