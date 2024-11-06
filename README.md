# megalith-micro

own blog...(A spring application with graalvm aot)

A Simple Cache Library both local and remote cache

```xml
<dependency>
    <groupId>wiki.chiu.megalith</groupId>
    <artifactId>cache-spring-boot-starter</artifactId>
    <version>1.7.0</version>
</dependency>
```

or

```kotlin
implementation("wiki.chiu.megalith:cache-spring-boot-starter:1.7.0")
```

use:

```java

@Autowired
private CacheEvictHandler cacheEvictHandler;

//use cache:
@GetMapping("/test")
@Cache(prefix = "12121212")
public String test() {
    return "adCustomRuntimeHintsadwdawdawdawda";
}

//evict cache:
@GetMapping("/evict")
public String evict() {
    HashSet<String> set = new HashSet<>();
    set.add("adCustomRuntimeHintsadwdawdawdawda");
    cacheEvictHandler.evictCache(set);
}
```

It can be updated to a stable queue(optional):

```yml
megalith:
  cache:
    topic: rabbit
    queue-prefix: cache.user.evict.queue.
    fanout-exchange: cache.user.evict.fanout.exchange
```
