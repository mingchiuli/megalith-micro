package org.chiu.micro.common.dto;

import java.io.Serializable;
import java.util.List;

public record BlogSensitiveContentRpcDto(

        Long blogId,

        List<SensitiveContentRpcDto> sensitiveContent) implements Serializable {

    public static BlogSensitiveContentDtoBuilder builder() {
        return new BlogSensitiveContentDtoBuilder();
    }

    public static class BlogSensitiveContentDtoBuilder {
        private Long blogId;
        private List<SensitiveContentRpcDto> sensitiveContentRpcDto;

        public BlogSensitiveContentDtoBuilder blogId(Long blogId) {
            this.blogId = blogId;
            return this;
        }

        public BlogSensitiveContentDtoBuilder sensitiveContent(List<SensitiveContentRpcDto> sensitiveContentRpcDto) {
            this.sensitiveContentRpcDto = sensitiveContentRpcDto;
            return this;
        }

        public BlogSensitiveContentRpcDto build() {
            return new BlogSensitiveContentRpcDto(blogId, sensitiveContentRpcDto);
        }
    }
}
