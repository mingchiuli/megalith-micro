package wiki.chiu.micro.search.api.vo;

import java.util.List;

public record RecommendationRpcVo(
    List<PublicBlogRpcVo> hot, List<PublicBlogRpcVo> related) {
}
