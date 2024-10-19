package org.chiu.micro.common.dto;

import java.io.Serializable;
import java.util.List;

public record BlogSensitiveContentRpcDto(

        Long blogId,

        List<SensitiveContentDto> sensitiveContentDto) implements Serializable {

    public static BlogSensitiveContentDtoBuilder builder() {
        return new BlogSensitiveContentDtoBuilder();
    }

    public static class BlogSensitiveContentDtoBuilder {
        private Long blogId;
        private List<SensitiveContentDto> sensitiveContentDto;

        public BlogSensitiveContentDtoBuilder blogId(Long blogId) {
            this.blogId = blogId;
            return this;
        }

        public BlogSensitiveContentDtoBuilder sensitiveContent(List<SensitiveContentDto> sensitiveContentDto) {
            this.sensitiveContentDto = sensitiveContentDto;
            return this;
        }

        public BlogSensitiveContentRpcDto build() {
            return new BlogSensitiveContentRpcDto(blogId, sensitiveContentDto);
        }
    }
}
