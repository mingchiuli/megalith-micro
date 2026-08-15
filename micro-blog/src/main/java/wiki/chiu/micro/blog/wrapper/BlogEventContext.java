package wiki.chiu.micro.blog.wrapper;

import wiki.chiu.micro.common.lang.BlogOperateEnum;

public record BlogEventContext(
    BlogOperateEnum operation, Long operatorUserId, long totalCount, Long newerOrSameCount) {}
