package wiki.chiu.micro.blog.application.model;

import wiki.chiu.micro.common.enums.BlogOperateEnum;

public record BlogEventContext(BlogOperateEnum operation, Long operatorUserId) {
}
