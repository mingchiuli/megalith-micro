package wiki.chiu.micro.exhibit.rpc;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.rpc.BlogHttpService;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.common.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.vo.BlogSensitiveContentRpcVo;

/** BlogHttpServiceWrapper */
@Component
public class BlogHttpServiceWrapper {

  private final BlogHttpService blogHttpService;

  public BlogHttpServiceWrapper(BlogHttpService blogHttpService) {
    this.blogHttpService = blogHttpService;
  }

  public BlogEntityRpcVo findById(Long blogId) {
    return RemoteResult.requireSuccess(() -> blogHttpService.findById(blogId));
  }

  public List<BlogEntityRpcVo> findAllById(List<Long> ids) {
    return RemoteResult.requireSuccess(() -> blogHttpService.findAllById(ids));
  }

  public Long count() {
    return RemoteResult.requireSuccess(blogHttpService::count);
  }

  public void setReadCount(Long id) {
    blogHttpService.setReadCount(id);
  }

  public PageAdapter<BlogEntityRpcVo> findPage(Integer pageNo, Integer pageSize) {
    return RemoteResult.requireSuccess(() -> blogHttpService.findPage(pageNo, pageSize));
  }

  public long countByCreatedGreaterThanEqual(LocalDateTime created) {
    return RemoteResult.requireSuccess(
        () -> blogHttpService.countByCreatedGreaterThanEqual(created));
  }

  public BlogSensitiveContentRpcVo findSensitiveByBlogId(Long blogId) {
    return RemoteResult.requireSuccess(() -> blogHttpService.findSensitiveByBlogId(blogId));
  }
}
