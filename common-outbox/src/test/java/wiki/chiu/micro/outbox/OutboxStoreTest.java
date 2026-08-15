package wiki.chiu.micro.outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class OutboxStoreTest {

  @Container
  static final MariaDBContainer<?> MARIA_DB =
      new MariaDBContainer<>("mariadb:11.4")
          .withDatabaseName("outbox")
          .withUsername("outbox")
          .withPassword("outbox")
          .withInitScript("m_outbox_event.sql");

  private OutboxStore store;

  @BeforeEach
  void setUp() {
    DriverManagerDataSource dataSource =
        new DriverManagerDataSource(
            MARIA_DB.getJdbcUrl(), MARIA_DB.getUsername(), MARIA_DB.getPassword());
    store = new OutboxStore(new JdbcTemplate(dataSource));
  }

  @Test
  void insertMakesRowReadyAndFindReadyReturnsIt() {
    store.insert("e1", OutboxProducer.BLOG, "blog", "1", "BlogChangedMessage", "{\"revision\":1}");

    List<OutboxRecord> ready = store.findReady(OutboxProducer.BLOG, 10);

    assertEquals(1, ready.size());
    OutboxRecord record = ready.getFirst();
    assertEquals("e1", record.eventId());
    assertEquals(OutboxProducer.BLOG, record.producer());
    assertEquals("blog", record.aggregateType());
    assertEquals("1", record.aggregateId());
    assertEquals("BlogChangedMessage", record.eventType());
    assertEquals("{\"revision\":1}", record.payload());
    assertEquals(0, record.attempts());
  }

  @Test
  void findReadyReturnsOnlyOldestPerAggregate() {
    store.insert("e1", OutboxProducer.BLOG, "blog", "7", "BlogChangedMessage", "{}");
    store.insert("e2", OutboxProducer.BLOG, "blog", "7", "BlogChangedMessage", "{}");
    store.insert("e3", OutboxProducer.USER, "user", "42", "AuthCacheEvictMessage", "{}");

    assertEquals(
        List.of("e1"),
        store.findReady(OutboxProducer.BLOG, 10).stream().map(OutboxRecord::eventId).toList());

    store.delete(store.findReady(OutboxProducer.BLOG, 10).getFirst().id(), OutboxProducer.BLOG);

    assertEquals(
        List.of("e2"),
        store.findReady(OutboxProducer.BLOG, 10).stream().map(OutboxRecord::eventId).toList());
    assertEquals(List.of(), store.findReady(OutboxProducer.USER, 10));
  }

  @Test
  void rescheduleMovesAvailableAtIntoFutureAndIncrementsAttempts() {
    store.insert("e1", OutboxProducer.BLOG, "blog", "7", "BlogChangedMessage", "{}");
    OutboxRecord record = store.findReady(OutboxProducer.BLOG, 10).getFirst();

    store.reschedule(
        record.id(), OutboxProducer.BLOG, LocalDateTime.now().plusMinutes(5), "broker nack");

    assertEquals(List.of(), store.findReady(OutboxProducer.BLOG, 10));
    assertEquals(1, store.status(OutboxProducer.BLOG).maximumAttempts());
  }

  @Test
  void deleteReturnsFalseForMissingRow() {
    assertFalse(store.delete(999L, OutboxProducer.BLOG));
  }

  @Test
  void statusAndManageCoverPauseResumeAndDiscard() {
    store.insert("e1", OutboxProducer.BLOG, "blog", "7", "BlogChangedMessage", "{}");
    store.insert("e2", OutboxProducer.BLOG, "blog", "7", "BlogChangedMessage", "{}");

    OutboxStatus initial = store.status(OutboxProducer.BLOG);
    assertEquals(2, initial.ready());
    assertEquals(0, initial.paused());

    assertTrue(store.manage("e1", OutboxProducer.BLOG, "pause"));
    OutboxStatus paused = store.status(OutboxProducer.BLOG);
    assertEquals(1, paused.ready());
    assertEquals(1, paused.paused());

    assertTrue(store.manage("e1", OutboxProducer.BLOG, "resume"));
    assertEquals(2, store.status(OutboxProducer.BLOG).ready());

    assertTrue(store.manage("e1", OutboxProducer.BLOG, "discard"));
    assertEquals(
        List.of("e2"),
        store.findReady(OutboxProducer.BLOG, 10).stream().map(OutboxRecord::eventId).toList());
  }
}
