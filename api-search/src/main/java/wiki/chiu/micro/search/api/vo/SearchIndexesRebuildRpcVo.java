package wiki.chiu.micro.search.api.vo;

import java.util.List;

public record SearchIndexesRebuildRpcVo(List<IndexRebuildRpcVo> indexes) {

    public SearchIndexesRebuildRpcVo {
        indexes = List.copyOf(indexes);
    }
}
