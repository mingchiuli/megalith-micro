package wiki.chiu.micro.common.message;

import wiki.chiu.micro.common.model.BlogSnapshot;

public record BlogChangedMessage(
    String eventId,
    Integer operation,
    Long revision,
    Long operatorUserId,
    BlogSnapshot blogSnapshot,
    Long adminRevision,
    Long previousPublishedRevisionId) {

    public BlogChangedMessage(
        String eventId, Integer operation, Long revision, Long operatorUserId,
        BlogSnapshot blogSnapshot, Long adminRevision) {
        this(eventId, operation, revision, operatorUserId, blogSnapshot, adminRevision, null);
    }

    public BlogChangedMessage(
        String eventId,
        Integer operation,
        Long revision,
        Long operatorUserId,
        BlogSnapshot blogSnapshot) {
        this(eventId, operation, revision, operatorUserId, blogSnapshot, null);
    }
}
