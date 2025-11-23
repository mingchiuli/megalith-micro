# Megalith Cache Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/wiki.chiu.megalith/cache-spring-boot-starter.svg)](https://search.maven.org/artifact/wiki.chiu.megalith/cache-spring-boot-starter)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/Java-24+-orange.svg)](https://openjdk.java.net/)

A high-performance, multi-level caching framework for Spring Boot applications that provides both local and distributed caching capabilities with automatic cache eviction support.

## ✨ Features

- **Multi-Level Caching**: Combines local (Caffeine) and remote (Redis) caching for optimal performance
- **Automatic Cache Eviction**: Supports distributed cache invalidation via RabbitMQ or Redis pub/sub
- **Spring Boot Integration**: Seamless auto-configuration with Spring Boot
- **AOP-Based**: Simple annotation-driven caching with `@Cache`
- **GraalVM Native Support**: Fully compatible with GraalVM native compilation
- **JPMS Module**: Proper Java Platform Module System support
- **High Performance**: Optimized for low latency and high throughput

## 🚀 Quick Start

### Installation

**Maven:**
```xml
<dependency>
    <groupId>wiki.chiu.megalith</groupId>
    <artifactId>cache-spring-boot-starter</artifactId>
    <version>${version}</version>
</dependency>
```

**Gradle:**
```kotlin
implementation("wiki.chiu.megalith:cache-spring-boot-starter:${version}")
```

### Basic Usage

```java
import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.cache.handler.CacheEvictHandler;
import wiki.chiu.micro.cache.utils.CommonCacheKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private CacheEvictHandler cacheEvictHandler;

    // Cache the result for 30 minutes (default)
    @GetMapping("/users/{id}")
    @Cache(prefix = "user", expire = 30)
    public User getUserById(@PathVariable Long id) {
        // This method result will be cached
        return userService.findById(id);
    }

    // Cache with custom expiration time (60 minutes)
    @GetMapping("/users")
    @Cache(prefix = "users", expire = 60)
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    // Manually evict cache
    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) throws Exception {
        userService.delete(id);

        // Evict related cache entries
        Method method = UserController.class.getMethod("getUserById", Long.class);
        HashSet<String> keys = CommonCacheKeyGenerator.generateKey(method, id);
        cacheEvictHandler.evictCache(keys);
    }
}
```

## ⚙️ Configuration

### Basic Configuration

```yaml
# Redis configuration (required)
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your-password
```

### Advanced Configuration with RabbitMQ

For distributed cache eviction across multiple application instances:

```yaml
# RabbitMQ configuration (optional)
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

# Cache eviction configuration
megalith:
  cache:
    queue-prefix: cache.evict.queue.
    fanout-exchange: cache.evict.fanout.exchange
```

## 📖 How It Works

### Cache Key Generation

Cache keys are automatically generated based on:
- Method signature
- Method parameters
- Custom prefix

The key generation logic can be found in `CommonCacheKeyGenerator.generateKey()`.

### Multi-Level Caching Strategy

1. **L1 Cache (Local)**: Caffeine in-memory cache for ultra-fast access
2. **L2 Cache (Remote)**: Redis for distributed caching
3. **Fallback**: Original method execution if cache miss

### Cache Eviction Strategies

- **Redis Pub/Sub**: Default distributed eviction mechanism
- **RabbitMQ**: More reliable eviction with message persistence (optional)

## 🔧 Advanced Features

### Custom Cache Key Generation

```java
@Cache(prefix = "custom")
public String customMethod(@CacheKey String param1, String param2) {
    // Only param1 will be included in cache key generation
    return "result";
}
```

### Conditional Caching

```java
@Cache(prefix = "conditional")
@Checker(handler = CustomCheckerHandler.class)
public String conditionalCache(String param) {
    // Cache only if CustomCheckerHandler allows it
    return "result";
}
```

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Application   │    │   Application   │    │   Application   │
│    Instance 1   │    │    Instance 2   │    │    Instance 3   │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────┴─────────────┐
                    │                           │
            ┌───────▼────────┐         ┌───────▼────────┐
            │     Redis      │         │    RabbitMQ    │
            │  (L2 Cache)    │         │ (Cache Evict)  │
            └────────────────┘         └────────────────┘
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.


