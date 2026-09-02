package wiki.chiu.micro.blog.api;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.blog.api.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;

public interface BlogHttpService {

    @GetExchange("/blog/ids")
    Result<List<Long>> findIdsAfter(
        @RequestParam Long afterId, @RequestParam Integer limit);

    @GetExchange("/blog/{blogId}")
    Result<BlogEntityRpcVo> findById(@PathVariable Long blogId);

    @PostExchange("/blog/batch")
    Result<List<BlogEntityRpcVo>> findAllById(@RequestBody List<Long> ids);

    @GetExchange("/blog/count")
    Result<Long> count();

    @PostExchange("/blog/{blogId}/views")
    Result<Void> setReadCount(@PathVariable Long blogId);

    @GetExchange("/blog/page")
    Result<PageAdapter<BlogEntityRpcVo>> findPage(
        @RequestParam Integer pageNo, @RequestParam Integer pageSize);

    @GetExchange("/blog/count/until")
    Result<Long> countByCreatedGreaterThanEqual(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime created);

    @GetExchange("/blog/sensitive/{blogId}")
    Result<BlogSensitiveContentRpcVo> findSensitiveByBlogId(@PathVariable Long blogId);
}
