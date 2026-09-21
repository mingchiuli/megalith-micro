package wiki.chiu.micro.common.outbox.application.model;

import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

public record OutboxStatus(OutboxProducer producer, long ready, long paused, int maximumAttempts) {
}
