package wiki.chiu.micro.common.lang;

public record BlogChangedMessage(
    String eventId,
    Integer operation,
    Long revision,
    BlogSnapshot blogSnapshot,
    Long totalCount,
    Long newerOrSameCount) {}
