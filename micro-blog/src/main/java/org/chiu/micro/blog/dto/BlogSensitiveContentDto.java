package org.chiu.micro.blog.dto;

import java.util.List;

public record BlogSensitiveContentDto(

        Long blogId,

        List<String> sensitiveContent) {


    public static BlogSensitiveContentDtoBuilder builder() {
        return new BlogSensitiveContentDtoBuilder();
    }

    public static class BlogSensitiveContentDtoBuilder {
        private Long blogId;
        private List<String> sensitiveContent;

        public BlogSensitiveContentDtoBuilder blogId(Long blogId) {
            this.blogId = blogId;
            return this;
        }

        public BlogSensitiveContentDtoBuilder sensitiveContent(List<String> sensitiveContent) {
            this.sensitiveContent = sensitiveContent;
            return this;
        }

        public BlogSensitiveContentDto build() {
            return new BlogSensitiveContentDto(blogId, sensitiveContent);
        }
    }
}
