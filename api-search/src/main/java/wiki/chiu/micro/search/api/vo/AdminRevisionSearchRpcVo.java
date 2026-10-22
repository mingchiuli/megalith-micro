package wiki.chiu.micro.search.api.vo;

import java.util.List;

public record AdminRevisionSearchRpcVo(
    Long total, Integer currentPage, Integer size, List<AdminRevisionRpcVo> content) {
}
