package org.chiu.micro.blog.vo;

import java.util.List;

public class BlogSensitiveContentVo {

    private Long blogId;

    private List<SensitiveContentVo> sensitiveContent;

    BlogSensitiveContentVo(Long blogId, List<SensitiveContentVo> sensitiveContent) {
        this.blogId = blogId;
        this.sensitiveContent = sensitiveContent;
    }

    public static BlogSensitiveContentVoBuilder builder() {
        return new BlogSensitiveContentVoBuilder();
    }

    public Long getBlogId() {
        return this.blogId;
    }

    public List<SensitiveContentVo> getSensitiveContent() {
        return this.sensitiveContent;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    public void setSensitiveContent(List<SensitiveContentVo> sensitiveContent) {
        this.sensitiveContent = sensitiveContent;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BlogSensitiveContentVo)) return false;
        final BlogSensitiveContentVo other = (BlogSensitiveContentVo) o;
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
        return other instanceof BlogSensitiveContentVo;
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
        return "BlogSensitiveContentVo(blogId=" + this.getBlogId() + ", sensitiveContent=" + this.getSensitiveContent() + ")";
    }

    public static class BlogSensitiveContentVoBuilder {
        private Long blogId;
        private List<SensitiveContentVo> sensitiveContent;

        BlogSensitiveContentVoBuilder() {
        }

        public BlogSensitiveContentVoBuilder blogId(Long blogId) {
            this.blogId = blogId;
            return this;
        }

        public BlogSensitiveContentVoBuilder sensitiveContent(List<SensitiveContentVo> sensitiveContent) {
            this.sensitiveContent = sensitiveContent;
            return this;
        }

        public BlogSensitiveContentVo build() {
            return new BlogSensitiveContentVo(this.blogId, this.sensitiveContent);
        }

        public String toString() {
            return "BlogSensitiveContentVo.BlogSensitiveContentVoBuilder(blogId=" + this.blogId + ", sensitiveContent=" + this.sensitiveContent + ")";
        }
    }
}
