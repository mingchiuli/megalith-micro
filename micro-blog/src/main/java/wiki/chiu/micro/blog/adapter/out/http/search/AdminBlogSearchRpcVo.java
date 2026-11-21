package wiki.chiu.micro.blog.adapter.out.http.search;

import java.util.List;

public record AdminBlogSearchRpcVo(
    Long total, Integer currentPage, Integer size, List<AdminBlogRpcVo> content) {
}
