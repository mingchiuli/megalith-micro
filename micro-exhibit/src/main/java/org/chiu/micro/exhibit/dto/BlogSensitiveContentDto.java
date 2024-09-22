package org.chiu.micro.exhibit.dto;

import java.io.Serializable;
import java.util.List;

public class BlogSensitiveContentDto implements Serializable {

    private Long blogId;

    private List<SensitiveContent> sensitiveContent;

    BlogSensitiveContentDto(Long blogId, List<SensitiveContent> sensitiveContent) {
        this.blogId = blogId;
        this.sensitiveContent = sensitiveContent;
    }

    public static BlogSensitiveContentDtoBuilder builder() {
        return new BlogSensitiveContentDtoBuilder();
    }

    public Long getBlogId() {
        return this.blogId;
    }

    public List<SensitiveContent> getSensitiveContent() {
        return this.sensitiveContent;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    public void setSensitiveContent(List<SensitiveContent> sensitiveContent) {
        this.sensitiveContent = sensitiveContent;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BlogSensitiveContentDto)) return false;
        final BlogSensitiveContentDto other = (BlogSensitiveContentDto) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$blogId = this.getBlogId();
        final Object other$blogId = other.getBlogId();
        if (this$blogId == null ? other$blogId != null : !this$blogId.equals(other$blogId)) return false;
        final Object this$sensitiveContent = this.getSensitiveContent();
        final Object other$sensitiveContent = other.getSensitiveContent();
        if (this$sensitiveContent == null ? other$sensitiveContent != null : !this$sensitiveContent.equals(other$sensitiveContent))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BlogSensitiveContentDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $blogId = this.getBlogId();
        result = result * PRIME + ($blogId == null ? 43 : $blogId.hashCode());
        final Object $sensitiveContent = this.getSensitiveContent();
        result = result * PRIME + ($sensitiveContent == null ? 43 : $sensitiveContent.hashCode());
        return result;
    }

    public String toString() {
        return "BlogSensitiveContentDto(blogId=" + this.getBlogId() + ", sensitiveContent=" + this.getSensitiveContent() + ")";
    }

    public static class BlogSensitiveContentDtoBuilder {
        private Long blogId;
        private List<SensitiveContent> sensitiveContent;

        BlogSensitiveContentDtoBuilder() {
        }

        public BlogSensitiveContentDtoBuilder blogId(Long blogId) {
            this.blogId = blogId;
            return this;
        }

        public BlogSensitiveContentDtoBuilder sensitiveContent(List<SensitiveContent> sensitiveContent) {
            this.sensitiveContent = sensitiveContent;
            return this;
        }

        public BlogSensitiveContentDto build() {
            return new BlogSensitiveContentDto(this.blogId, this.sensitiveContent);
        }

        public String toString() {
            return "BlogSensitiveContentDto.BlogSensitiveContentDtoBuilder(blogId=" + this.blogId + ", sensitiveContent=" + this.sensitiveContent + ")";
        }
    }
}
