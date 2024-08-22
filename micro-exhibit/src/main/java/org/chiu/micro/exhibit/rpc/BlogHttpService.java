package org.chiu.micro.exhibit.rpc;

import java.time.LocalDateTime;
import java.util.List;

import org.chiu.micro.exhibit.dto.BlogEntityDto;
import org.chiu.micro.exhibit.dto.BlogSensitiveContentDto;
import org.chiu.micro.exhibit.lang.Result;
import org.chiu.micro.exhibit.page.PageAdapter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

public interface BlogHttpService {

    @GetExchange("/blog/{blogId}")
    Result<BlogEntityDto> findById(@PathVariable Long blogId);

    @PostExchange("/blog/batch")
    Result<List<BlogEntityDto>> findAllById(@RequestBody List<Long> ids);

    @GetExchange("/blog/years")
    Result<List<Integer>> getYears();

    @GetExchange("/blog/count")
    Result<Long> count();

    @GetExchange("/blog/ids/{pageNo}/{pageSize}")
    Result<List<Long>> findIds(@PathVariable(value = "pageNo") Integer pageNo,
                               @PathVariable(value = "pageSize") Integer pageSize);

    @PostExchange("/blog/{blogId}")
    void setReadCount(@PathVariable Long blogId);

    @GetExchange("/blog/status/{blogId}")
    Result<Integer> findStatusById(@PathVariable Long blogId);

    @PostExchange("/blog/page/{pageNo}/{pageSize}")
    Result<PageAdapter<BlogEntityDto>> findPage(@PathVariable(value = "pageNo") Integer pageNo,
                                                @PathVariable(value = "pageSize") Integer pageSize);

    @PostExchange("/blog/page/year/{pageNo}/{pageSize}/{start}/{end}")
    Result<PageAdapter<BlogEntityDto>> findPageByCreatedBetween(@PathVariable(value = "pageNo") Integer pageNo,
                                                                @PathVariable(value = "pageSize") Integer pageSize,
                                                                @PathVariable(value = "start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                                                @PathVariable(value = "end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end);

    @GetExchange("/blog/count/{start}/{end}")
    Result<Long> countByCreatedBetween(@PathVariable(value = "start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                       @PathVariable(value = "end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end);

    @GetExchange("/blog/page/count/year/{created}/{start}/{end}")
    Result<Long> getPageCountYear(@PathVariable(value = "created") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime created,
                                  @PathVariable(value = "start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                  @PathVariable(value = "end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end);

    @GetExchange("/blog/count/until/{created}")
    Result<Long> countByCreatedGreaterThanEqual(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime created);

    @GetExchange("/blog/sensitive/{blogId}")
    Result<BlogSensitiveContentDto> findSensitiveByBlogId(@PathVariable Long blogId);
}
