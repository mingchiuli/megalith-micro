package wiki.chiu.micro.search.api;

import org.springframework.web.service.annotation.PostExchange;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.search.api.vo.IndexRebuildRpcVo;

public interface SearchIndexMaintenanceHttpService {

    @PostExchange("/search/index/rebuild")
    Result<IndexRebuildRpcVo> rebuild();
}
