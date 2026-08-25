/**
 * Megalith Cache Spring Boot Starter Module
 *
 * <p>A cache framework that provides both local and remote caching capabilities with cache eviction
 * support through RabbitMQ or Redis pub/sub.
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
  opens wiki.chiu.micro.cache.aspect;
  opens wiki.chiu.micro.cache.listener;
  opens wiki.chiu.micro.cache.handler.impl;
  opens wiki.chiu.micro.cache.aot.hints;
  opens wiki.chiu.micro.cache.key.impl;
  opens wiki.chiu.micro.cache.message;
}
