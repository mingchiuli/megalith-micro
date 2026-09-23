/**
 * Megalith Cache Spring Boot Starter Module
 *
 * <p>A cache framework that provides both local and remote caching capabilities with cache eviction
 * support through RabbitMQ or Redis pub/sub.
 *
 * <p>The published API is {@code annotation}, {@code handler}, and {@code key}. Internals follow
 * the application layout: read-through caching is an {@code adapter.in.aop} adapter, eviction
 * transports are {@code adapter.in.messaging} and {@code adapter.out.eviction} adapters, and
 * shared keys, models, and wiring live in {@code application} and {@code config}.
 *
 * @author mingchiuli
 * @since 2025
 */
module wiki.chiu.micro.cache {
    requires org.slf4j;
    requires spring.core;
    requires spring.aop;
    requires org.aspectj.weaver;
    requires redisson;
    requires com.github.benmanes.caffeine;
    requires micrometer.core;
    requires spring.beans;
    requires spring.context;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires static spring.amqp;
    requires static spring.rabbit;
    requires static spring.boot.amqp;
    requires jakarta.annotation;
    requires org.jspecify;
    requires tools.jackson.databind;

    // Public API packages - used by downstream modules
    exports wiki.chiu.micro.cache.annotation;
    exports wiki.chiu.micro.cache.handler;
    exports wiki.chiu.micro.cache.key;

    // Implementation packages - opened for Spring reflection only
    opens wiki.chiu.micro.cache.config;
    opens wiki.chiu.micro.cache.aot;
    opens wiki.chiu.micro.cache.application.model;
    opens wiki.chiu.micro.cache.adapter.in.aop;
    opens wiki.chiu.micro.cache.adapter.in.messaging;
    opens wiki.chiu.micro.cache.adapter.out.eviction;
    opens wiki.chiu.micro.cache.adapter.out.key;
}
