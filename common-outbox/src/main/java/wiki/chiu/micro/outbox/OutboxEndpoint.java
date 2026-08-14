package wiki.chiu.micro.outbox;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import wiki.chiu.micro.outbox.config.OutboxProperties;

@Endpoint(id = "outbox")
public class OutboxEndpoint {

  private final OutboxStore store;
  private final OutboxProperties properties;
  private final RedissonClient redissonClient;

  public OutboxEndpoint(
      OutboxStore store, OutboxProperties properties, RedissonClient redissonClient) {
    this.store = store;
    this.properties = properties;
    this.redissonClient = redissonClient;
  }

  @ReadOperation
  public OutboxStatus status() {
    return store.status(properties.getProducer());
  }

  @WriteOperation
  public boolean manage(@Selector String eventId, String action) {
    RLock lock = redissonClient.getLock(OutboxLockNames.publisher(properties));
    lock.lock();
    try {
      return store.manage(eventId, properties.getProducer(), action);
    } finally {
      lock.unlock();
    }
  }
}
