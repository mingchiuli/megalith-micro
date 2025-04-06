package wiki.chiu.micro.common.vo;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public record BlogSensitiveContentRpcVo(
        Long blogId,
        List<SensitiveContentRpcVo> sensitiveContent) implements Serializable {

    public static BlogSensitiveContentRpcVoBuilder builder() {
        return new BlogSensitiveContentRpcVoBuilder(null, Collections.emptyList());
    }

    public record BlogSensitiveContentRpcVoBuilder(
            Long blogId,
            List<SensitiveContentRpcVo> sensitiveContent
    ) {

        public BlogSensitiveContentRpcVoBuilder blogId(Long blogId) {
            return new BlogSensitiveContentRpcVoBuilder(blogId, this.sensitiveContent);
        }

        public BlogSensitiveContentRpcVoBuilder sensitiveContent(List<SensitiveContentRpcVo> sensitiveContent) {
            return new BlogSensitiveContentRpcVoBuilder(this.blogId, sensitiveContent);
        }

        public BlogSensitiveContentRpcVo build() {
            return new BlogSensitiveContentRpcVo(blogId, sensitiveContent);
        }
    }
}