package wiki.chiu.micro.blog.application.model;

import wiki.chiu.micro.common.lang.BlogOperateEnum;

public record BlogEventContext(BlogOperateEnum operation, Long operatorUserId) {
}
