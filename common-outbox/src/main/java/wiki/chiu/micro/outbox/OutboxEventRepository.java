package wiki.chiu.micro.outbox;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {

    @Transactional(readOnly = true)
    @Query(
        """
            SELECT e FROM OutboxEventEntity e
            WHERE e.producer = :producer
              AND e.state = 'READY'
              AND e.availableAt <= :now
              AND NOT EXISTS (
                SELECT 1 FROM OutboxEventEntity p
                WHERE p.producer = e.producer
                  AND p.aggregateType = e.aggregateType
                  AND p.aggregateId = e.aggregateId
                  AND p.id < e.id)
            ORDER BY e.availableAt, e.id
            """)
    List<OutboxEventEntity> findReady(
        @Param("producer") OutboxProducer producer,
        @Param("now") LocalDateTime now,
        Pageable pageable);

    long countByProducerAndState(OutboxProducer producer, String state);

    @Query(
        """
            SELECT COALESCE(MAX(e.attempts), 0)
            FROM OutboxEventEntity e
            WHERE e.producer = :producer
            """)
    int maxAttempts(@Param("producer") OutboxProducer producer);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM OutboxEventEntity e WHERE e.id = :id AND e.producer = :producer")
    int deleteByIdAndProducer(@Param("id") long id, @Param("producer") OutboxProducer producer);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
        """
            UPDATE OutboxEventEntity e
            SET e.attempts = e.attempts + 1, e.availableAt = :availableAt, e.lastError = :error
            WHERE e.id = :id AND e.producer = :producer AND e.state = 'READY'
            """)
    int reschedule(
        @Param("id") long id,
        @Param("producer") OutboxProducer producer,
        @Param("availableAt") LocalDateTime availableAt,
        @Param("error") String error);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
        """
            UPDATE OutboxEventEntity e
            SET e.state = 'PAUSED'
            WHERE e.eventId = :eventId AND e.producer = :producer AND e.state = 'READY'
            """)
    int pause(@Param("eventId") String eventId, @Param("producer") OutboxProducer producer);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
        """
            UPDATE OutboxEventEntity e
            SET e.state = 'READY', e.availableAt = :now
            WHERE e.eventId = :eventId AND e.producer = :producer
            """)
    int resume(
        @Param("eventId") String eventId,
        @Param("producer") OutboxProducer producer,
        @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM OutboxEventEntity e WHERE e.eventId = :eventId AND e.producer = :producer")
    int discard(@Param("eventId") String eventId, @Param("producer") OutboxProducer producer);
}
