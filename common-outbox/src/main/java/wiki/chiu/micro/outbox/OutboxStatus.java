package wiki.chiu.micro.outbox;

public record OutboxStatus(OutboxProducer producer, long ready, long paused, int maximumAttempts) {}
