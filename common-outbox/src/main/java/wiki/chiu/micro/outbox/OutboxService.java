package wiki.chiu.micro.outbox;

import java.util.UUID;
import java.util.function.Function;
import tools.jackson.databind.json.JsonMapper;

public class OutboxService {

  private final OutboxStore store;
  private final JsonMapper jsonMapper;

  public OutboxService(OutboxStore store, JsonMapper jsonMapper) {
    this.store = store;
    this.jsonMapper = jsonMapper;
  }

  public String enqueue(
      OutboxProducer producer, String aggregateType, Object aggregateId, Object event) {
    String eventId = UUID.randomUUID().toString();
    insert(eventId, producer, aggregateType, aggregateId, event);
    return eventId;
  }

  public String enqueue(
      OutboxProducer producer,
      String aggregateType,
      Object aggregateId,
      Function<String, Object> eventFactory) {
    String eventId = UUID.randomUUID().toString();
    insert(eventId, producer, aggregateType, aggregateId, eventFactory.apply(eventId));
    return eventId;
  }

  private void insert(
      String eventId,
      OutboxProducer producer,
      String aggregateType,
      Object aggregateId,
      Object event) {
    store.insert(
        eventId,
        producer,
        aggregateType,
        String.valueOf(aggregateId),
        event.getClass().getSimpleName(),
        jsonMapper.writeValueAsString(event));
  }
}
