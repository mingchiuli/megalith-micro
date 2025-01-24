# megalith-micro

own blog...(A spring application with graalvm aot)

A Simple Cache Library both local and redis remote cache

```xml
<dependency>
    <groupId>wiki.chiu.megalith</groupId>
    <artifactId>cache-spring-boot-starter</artifactId>
    <version>3.4.2</version>
</dependency>
```

or

```kotlin
implementation("wiki.chiu.megalith:cache-spring-boot-starter:3.4.1")
```

use:

```java
import java.lang.reflect.Method;
import java.util.HashSet;

@RestController
public class MyClass {
    
    @Autowired
    private CacheEvictHandler cacheEvictHandler;

    //use cache:
    @GetMapping("/test")
    @Cache(prefix = "keyPrefix")
    public String test() {
        return "this is a value";
    }

    //evict cache:
    @GetMapping("/evict")
    public String evict() {
        Method method = MyClass.class.getMethod("test");
        HashSet<String> keys = CommonCacheKeyGenerator.generateKey(method);
        cacheEvictHandler.evictCache(keys);
    }
}

```

The rule of the generation of key can be find in class `CommonCacheKeyGenerator::generateKey`

It can be auto upgraded to a stable queue(if you used rabbitmq) via config and rabbitmq dependencies(default setting is redis):

```yml
megalith:
  cache:
    queue-prefix: cache.user.evict.queue.
    fanout-exchange: cache.user.evict.fanout.exchange
```


