package wiki.chiu.micro.outbox;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

public class OutboxStore {

  private final JdbcTemplate jdbcTemplate;

  public OutboxStore(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void insert(
      String eventId,
      OutboxProducer producer,
      String aggregateType,
      String aggregateId,
      String eventType,
      String payload) {
    jdbcTemplate.update(
        """
        INSERT INTO m_outbox_event
          (event_id, producer, aggregate_type, aggregate_id, event_type, payload,
           state, attempts, available_at, created_at)
        VALUES (?, ?, ?, ?, ?, ?, 'READY', 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
        """,
        eventId,
        producer.name(),
        aggregateType,
        aggregateId,
        eventType,
        payload);
  }

  public List<OutboxRecord> findReady(OutboxProducer producer, int limit) {
    return jdbcTemplate.query(
        """
        SELECT id, event_id, producer, aggregate_type, aggregate_id, event_type,
               payload, attempts, available_at
        FROM m_outbox_event event
        WHERE event.producer = ?
          AND event.state = 'READY'
          AND event.available_at <= CURRENT_TIMESTAMP(6)
          AND NOT EXISTS (
            SELECT 1
            FROM m_outbox_event predecessor
            WHERE predecessor.producer = event.producer
              AND predecessor.aggregate_type = event.aggregate_type
              AND predecessor.aggregate_id = event.aggregate_id
              AND predecessor.id < event.id
          )
        ORDER BY event.available_at, event.id
        LIMIT ?
        """,
        (rs, _) ->
            new OutboxRecord(
                rs.getLong("id"),
                rs.getString("event_id"),
                OutboxProducer.valueOf(rs.getString("producer")),
                rs.getString("aggregate_type"),
                rs.getString("aggregate_id"),
                rs.getString("event_type"),
                rs.getString("payload"),
                rs.getInt("attempts"),
                rs.getTimestamp("available_at").toLocalDateTime()),
        producer.name(),
        limit);
  }

  public boolean delete(long id, OutboxProducer producer) {
    return jdbcTemplate.update(
            "DELETE FROM m_outbox_event WHERE id = ? AND producer = ?", id, producer.name())
        == 1;
  }

  public void reschedule(
      long id, OutboxProducer producer, LocalDateTime availableAt, String error) {
    jdbcTemplate.update(
        """
        UPDATE m_outbox_event
        SET attempts = attempts + 1, available_at = ?, last_error = ?
        WHERE id = ? AND producer = ? AND state = 'READY'
        """,
        Timestamp.valueOf(availableAt),
        truncate(error),
        id,
        producer.name());
  }

  public long pendingCount(OutboxProducer producer) {
    Long value =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM m_outbox_event WHERE producer = ? AND state = 'READY'",
            Long.class,
            producer.name());
    return value == null ? 0 : value;
  }

  public OutboxStatus status(OutboxProducer producer) {
    Long ready = countByState(producer, "READY");
    Long paused = countByState(producer, "PAUSED");
    Integer maxAttempts =
        jdbcTemplate.queryForObject(
            "SELECT COALESCE(MAX(attempts), 0) FROM m_outbox_event WHERE producer = ?",
            Integer.class,
            producer.name());
    return new OutboxStatus(
        producer,
        ready == null ? 0 : ready,
        paused == null ? 0 : paused,
        maxAttempts == null ? 0 : maxAttempts);
  }

  public boolean manage(String eventId, OutboxProducer producer, String action) {
    return switch (action.toLowerCase(java.util.Locale.ROOT)) {
      case "pause" ->
          jdbcTemplate.update(
                  "UPDATE m_outbox_event SET state = 'PAUSED' WHERE event_id = ? AND producer = ? AND state = 'READY'",
                  eventId,
                  producer.name())
              == 1;
      case "resume", "retry" ->
          jdbcTemplate.update(
                  "UPDATE m_outbox_event SET state = 'READY', available_at = CURRENT_TIMESTAMP(6) WHERE event_id = ? AND producer = ?",
                  eventId,
                  producer.name())
              == 1;
      case "discard" ->
          jdbcTemplate.update(
                  "DELETE FROM m_outbox_event WHERE event_id = ? AND producer = ?",
                  eventId,
                  producer.name())
              == 1;
      default -> throw new IllegalArgumentException("Unsupported outbox action: " + action);
    };
  }

  private Long countByState(OutboxProducer producer, String state) {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM m_outbox_event WHERE producer = ? AND state = ?",
        Long.class,
        producer.name(),
        state);
  }

  private String truncate(String error) {
    if (error == null) {
      return "unknown publish failure";
    }
    return error.length() <= 2000 ? error : error.substring(0, 2000);
  }
}
