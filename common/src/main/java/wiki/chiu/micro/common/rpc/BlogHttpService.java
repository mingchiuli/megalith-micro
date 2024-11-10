package wiki.chiu.micro.common.rpc;

import wiki.chiu.micro.common.dto.BlogEntityRpcDto;
import wiki.chiu.micro.common.dto.BlogSensitiveContentRpcDto;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.time.LocalDateTime;
import java.util.List;

public interface BlogHttpService {

    @GetExchange("/blog/{blogId}")
    Result<BlogEntityRpcDto> findById(@PathVariable Long blogId);

    @PostExchange("/blog/batch")
    Result<List<BlogEntityRpcDto>> findAllById(@RequestBody List<Long> ids);

    @GetExchange("/blog/years")
    Result<List<Integer>> getYears();

    @GetExchange("/blog/count")
    Result<Long> count();

    @PostExchange("/blog/{blogId}")
    void setReadCount(@PathVariable Long blogId);

    @GetExchange("/blog/status/{blogId}")
    Result<Integer> findStatusById(@PathVariable Long blogId);

    @PostExchange("/blog/page/{pageNo}/{pageSize}")
    Result<PageAdapter<BlogEntityRpcDto>> findPage(@PathVariable Integer pageNo,
                                                @PathVariable Integer pageSize);

    @PostExchange("/blog/page/year/{pageNo}/{pageSize}/{start}/{end}")
    Result<PageAdapter<BlogEntityRpcDto>> findPageByCreatedBetween(@PathVariable Integer pageNo,
                                                                   @PathVariable Integer pageSize,
                                                                   @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                                   @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end);

    @GetExchange("/blog/count/{start}/{end}")
    Result<Long> countByCreatedBetween(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                       @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end);

    @GetExchange("/blog/page/count/year/{created}/{start}/{end}")
    Result<Long> getPageCountYear(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime created,
                                  @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                  @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end);

    @GetExchange("/blog/count/until/{created}")
    Result<Long> countByCreatedGreaterThanEqual(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime created);

    @GetExchange("/blog/sensitive/{blogId}")
    Result<BlogSensitiveContentRpcDto> findSensitiveByBlogId(@PathVariable Long blogId);
}
