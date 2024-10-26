package wiki.chiu.micro.blog.vo;

import java.util.List;

public record BlogSensitiveContentVo(
        Long blogId,

        List<SensitiveContentVo> sensitiveContent) {

    public static BlogSensitiveContentVoBuilder builder() {
        return new BlogSensitiveContentVoBuilder();
    }

    public static class BlogSensitiveContentVoBuilder {
        private Long blogId;
        private List<SensitiveContentVo> sensitiveContent;

        public BlogSensitiveContentVoBuilder blogId(Long blogId) {
            this.blogId = blogId;
            return this;
        }

        public BlogSensitiveContentVoBuilder sensitiveContent(List<SensitiveContentVo> sensitiveContent) {
            this.sensitiveContent = sensitiveContent;
            return this;
        }

        public BlogSensitiveContentVo build() {
            return new BlogSensitiveContentVo(blogId, sensitiveContent);
        }

    }
}
