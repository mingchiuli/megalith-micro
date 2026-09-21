package wiki.chiu.micro.common.message;

import wiki.chiu.micro.common.model.BlogSnapshot;

public record BlogChangedMessage(
    String eventId,
    Integer operation,
    Long revision,
    Long operatorUserId,
    BlogSnapshot blogSnapshot) {
}
