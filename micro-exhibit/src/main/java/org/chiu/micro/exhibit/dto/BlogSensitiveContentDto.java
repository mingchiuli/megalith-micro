package org.chiu.micro.exhibit.dto;

import java.io.Serializable;
import java.util.List;

public record BlogSensitiveContentDto(

        Long blogId,

        List<SensitiveContent> sensitiveContent) implements Serializable {

    public static BlogSensitiveContentDtoBuilder builder() {
        return new BlogSensitiveContentDtoBuilder();
    }

    public static class BlogSensitiveContentDtoBuilder {
        private Long blogId;
        private List<SensitiveContent> sensitiveContent;

        public BlogSensitiveContentDtoBuilder blogId(Long blogId) {
            this.blogId = blogId;
            return this;
        }

        public BlogSensitiveContentDtoBuilder sensitiveContent(List<SensitiveContent> sensitiveContent) {
            this.sensitiveContent = sensitiveContent;
            return this;
        }

        public BlogSensitiveContentDto build() {
            return new BlogSensitiveContentDto(blogId, sensitiveContent);
        }
    }
}
