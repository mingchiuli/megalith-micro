package wiki.chiu.micro.common.vo;

import java.io.Serializable;
import java.util.List;

public record BlogSensitiveContentRpcVo(

        Long blogId,

        List<SensitiveContentRpcVo> sensitiveContent) implements Serializable {

    public static BlogSensitiveContentDtoBuilder builder() {
        return new BlogSensitiveContentDtoBuilder();
    }

    public static class BlogSensitiveContentDtoBuilder {
        private Long blogId;
        private List<SensitiveContentRpcVo> sensitiveContentRpcVo;

        public BlogSensitiveContentDtoBuilder blogId(Long blogId) {
            this.blogId = blogId;
            return this;
        }

        public BlogSensitiveContentDtoBuilder sensitiveContent(List<SensitiveContentRpcVo> sensitiveContentRpcVo) {
            this.sensitiveContentRpcVo = sensitiveContentRpcVo;
            return this;
        }

        public BlogSensitiveContentRpcVo build() {
            return new BlogSensitiveContentRpcVo(blogId, sensitiveContentRpcVo);
        }
    }
}
