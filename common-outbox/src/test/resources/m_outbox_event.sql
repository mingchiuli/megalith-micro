CREATE TABLE m_outbox_event (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_id      VARCHAR(64)  NOT NULL,
  producer      VARCHAR(16)  NOT NULL,
  aggregate_type VARCHAR(32) NOT NULL,
  aggregate_id  VARCHAR(64)  NOT NULL,
  event_type    VARCHAR(64)  NOT NULL,
  payload       TEXT         NOT NULL,
  state         VARCHAR(16)  NOT NULL DEFAULT 'READY',
  attempts      INT          NOT NULL DEFAULT 0,
  available_at  TIMESTAMP(6) NOT NULL,
  created_at    TIMESTAMP(6) NOT NULL,
  last_error    VARCHAR(2000)
);

CREATE INDEX idx_outbox_poll ON m_outbox_event (producer, state, available_at);
