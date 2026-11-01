package wiki.chiu.micro.search.api;

import org.springframework.web.service.annotation.PostExchange;

import wiki.chiu.micro.common.result.Result;
import wiki.chiu.micro.search.api.vo.SearchIndexesRebuildRpcVo;

public interface SearchIndexMaintenanceHttpService {

    @PostExchange("/search/index/rebuild")
    Result<SearchIndexesRebuildRpcVo> rebuild();
}
