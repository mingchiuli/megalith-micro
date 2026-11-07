package wiki.chiu.micro.exhibit.application.model;

import wiki.chiu.micro.common.enums.BlogOperateEnum;

public record BlogChange(Long blogId, BlogOperateEnum operation, Long revision, Integer status) {
}
