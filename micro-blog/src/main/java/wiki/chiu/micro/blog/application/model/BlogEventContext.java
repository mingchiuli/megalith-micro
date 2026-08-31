package wiki.chiu.micro.blog.application.model;

import wiki.chiu.micro.common.lang.BlogOperateEnum;

public record BlogEventContext(
    BlogOperateEnum operation,
    Long operatorUserId,
    long totalCount,
    Long newerOrSameCount,
    Long previousTotalCount) {

    public BlogEventContext(
        BlogOperateEnum operation, Long operatorUserId, long totalCount, Long newerOrSameCount) {
        this(operation, operatorUserId, totalCount, newerOrSameCount, null);
    }
}
