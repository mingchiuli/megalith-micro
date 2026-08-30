package wiki.chiu.micro.common.lang;

public record BlogChangedMessage(
    String eventId,
    Integer operation,
    Long revision,
    Long operatorUserId,
    BlogSnapshot blogSnapshot,
    Long totalCount,
    Long newerOrSameCount,
    Long previousTotalCount) {

  public BlogChangedMessage(
      String eventId,
      Integer operation,
      Long revision,
      Long operatorUserId,
      BlogSnapshot blogSnapshot,
      Long totalCount,
      Long newerOrSameCount) {
    this(
        eventId,
        operation,
        revision,
        operatorUserId,
        blogSnapshot,
        totalCount,
        newerOrSameCount,
        null);
  }
}
