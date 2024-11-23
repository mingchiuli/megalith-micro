# megalith-micro

own blog...(A spring application with graalvm aot)

A Simple Cache Library both local and remote cache

```xml
<dependency>
    <groupId>wiki.chiu.megalith</groupId>
    <artifactId>cache-spring-boot-starter</artifactId>
    <version>3.3.5</version>
</dependency>
```

or

```kotlin
implementation("wiki.chiu.megalith:cache-spring-boot-starter:3.3.5")
```

use:

```java

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
    HashSet<String> set = new HashSet<>();
    //add the generated keys
    set.add("keyPrefix::XxxController::test");
    cacheEvictHandler.evictCache(set);
}
```

It can be auto upgraded to a stable queue(if you used rabbitmq) via config and rabbitmq dependancies:

```yml
megalith:
  cache:
    queue-prefix: cache.user.evict.queue.
    fanout-exchange: cache.user.evict.fanout.exchange
```


