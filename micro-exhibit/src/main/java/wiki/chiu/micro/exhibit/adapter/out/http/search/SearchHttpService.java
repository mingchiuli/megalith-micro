package wiki.chiu.micro.exhibit.adapter.out.http.search;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import wiki.chiu.micro.common.result.Result;

public interface SearchHttpService {

    @GetExchange("/blog/public/recommendations/feed")
    Result<RecommendationFeedRpcVo> recommendationFeed(
        @RequestParam(required = false) String cursor, @RequestParam Integer size);
}
