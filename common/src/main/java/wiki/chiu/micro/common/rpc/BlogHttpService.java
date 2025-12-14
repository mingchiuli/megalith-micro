package wiki.chiu.micro.common.rpc;

import org.springframework.web.bind.annotation.RequestParam;
import wiki.chiu.micro.common.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.time.LocalDateTime;
import java.util.List;

public interface BlogHttpService {

    @GetExchange("/blog/{blogId}")
    Result<BlogEntityRpcVo> findById(@PathVariable Long blogId);

    @PostExchange("/blog/batch")
    Result<List<BlogEntityRpcVo>> findAllById(@RequestBody List<Long> ids);

    @GetExchange("/blog/count")
    Result<Long> count();

    @PostExchange("/blog/{blogId}")
    Result<Void> setReadCount(@PathVariable Long blogId);

    @PostExchange("/blog/page")
    Result<PageAdapter<BlogEntityRpcVo>> findPage(@RequestParam Integer pageNo,
                                                  @RequestParam Integer pageSize);

    @GetExchange("/blog/count/until")
    Result<Long> countByCreatedGreaterThanEqual(@RequestParam LocalDateTime created);

    @GetExchange("/blog/sensitive/{blogId}")
    Result<BlogSensitiveContentRpcVo> findSensitiveByBlogId(@PathVariable Long blogId);
}
