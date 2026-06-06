/**
 * Megalith Cache Spring Boot Starter Module
 *
 * A cache framework that provides both local and remote caching capabilities
 * with cache eviction support through RabbitMQ or Redis pub/sub.
 *
 * @author mingchiuli
 * @since 2025
 */
module wiki.chiu.micro.cache {
    requires org.slf4j;
    requires spring.core;
    requires org.aspectj.weaver;
    requires transitive redisson;
    requires com.github.benmanes.caffeine;
    requires spring.beans;
    requires spring.context;
    requires static spring.boot.autoconfigure;
    requires static spring.amqp;
    requires static spring.rabbit;
    requires static spring.boot.amqp;
    requires jakarta.annotation;
    requires org.jspecify;
    requires static tools.jackson.databind;

    // Public API packages - used by downstream modules
    exports wiki.chiu.micro.cache.annotation;
    exports wiki.chiu.micro.cache.handler;
    exports wiki.chiu.micro.cache.utils;

    // Implementation packages - opened for Spring reflection only
    opens wiki.chiu.micro.cache.config;
    opens wiki.chiu.micro.cache.aspect;
    opens wiki.chiu.micro.cache.listener;
    opens wiki.chiu.micro.cache.handler.impl;
    opens wiki.chiu.micro.cache.aot.hints;
}