package wiki.chiu.micro.search.api.vo;

import java.util.List;

public record AdminBlogSearchRpcVo(
    Long total, Integer currentPage, Integer size, List<AdminBlogRpcVo> content) {
}
