package wiki.chiu.micro.blog.adapter.out.http.search;

import java.util.List;

public record AdminRevisionSearchRpcVo(
    Long total, Integer currentPage, Integer size, List<AdminRevisionRpcVo> content) {
}
