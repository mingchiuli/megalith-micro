package wiki.chiu.micro.blog.api;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;
import wiki.chiu.micro.common.lang.BlogSnapshot;
import wiki.chiu.micro.common.lang.Result;

public interface BlogIndexSourceHttpService {

    @GetExchange("/blog/index/status")
    Result<BlogIndexSourceStatus> indexSourceStatus();

    @GetExchange("/blog/index/snapshots")
    Result<List<BlogSnapshot>> indexSnapshots(@RequestParam long afterId, @RequestParam int limit);
}
