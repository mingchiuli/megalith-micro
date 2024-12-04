package wiki.chiu.micro.common.rpc;

import org.springframework.web.bind.annotation.RequestParam;
import wiki.chiu.micro.common.dto.BlogEntityRpcDto;
import wiki.chiu.micro.common.dto.BlogSensitiveContentRpcDto;
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

    @PostExchange("/blog/page")
    Result<PageAdapter<BlogEntityRpcDto>> findPage(@RequestParam Integer pageNo,
                                                   @RequestParam Integer pageSize);

    @PostExchange("/blog/page/year")
    Result<PageAdapter<BlogEntityRpcDto>> findPageByCreatedBetween(@RequestParam Integer pageNo,
                                                                   @RequestParam Integer pageSize,
                                                                   @RequestParam LocalDateTime start,
                                                                   @RequestParam LocalDateTime end);

    @GetExchange("/blog/count/year")
    Result<Long> countByCreatedBetween(@RequestParam LocalDateTime start,
                                       @RequestParam LocalDateTime end);

    @GetExchange("/blog/page/count/year")
    Result<Long> getPageCountYear(@RequestParam LocalDateTime created,
                                  @RequestParam LocalDateTime start,
                                  @RequestParam LocalDateTime end);

    @GetExchange("/blog/count/until/{created}")
    Result<Long> countByCreatedGreaterThanEqual(@RequestParam LocalDateTime created);

    @GetExchange("/blog/sensitive/{blogId}")
    Result<BlogSensitiveContentRpcDto> findSensitiveByBlogId(@PathVariable Long blogId);
}
