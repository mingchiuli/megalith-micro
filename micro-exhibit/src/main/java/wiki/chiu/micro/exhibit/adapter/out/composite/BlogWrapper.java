package wiki.chiu.micro.exhibit.adapter.out.composite;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.exhibit.adapter.out.http.BlogHttpServiceWrapper;
import wiki.chiu.micro.exhibit.adapter.out.http.SearchHttpServiceWrapper;
import wiki.chiu.micro.exhibit.adapter.out.http.UserHttpServiceWrapper;
import wiki.chiu.micro.exhibit.application.port.out.BlogReader;
import wiki.chiu.micro.exhibit.cache.BlogCacheDescriptors;
import wiki.chiu.micro.exhibit.convertor.BlogDescriptionDtoConvertor;
import wiki.chiu.micro.exhibit.convertor.BlogExhibitDtoConvertor;
import wiki.chiu.micro.exhibit.dto.BlogDescriptionDto;
import wiki.chiu.micro.exhibit.dto.BlogExhibitDto;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

@Component
public class BlogWrapper implements BlogReader {

  private final BlogHttpServiceWrapper blogHttpServiceWrapper;

  private final UserHttpServiceWrapper userHttpServiceWrapper;

  private final SearchHttpServiceWrapper searchHttpServiceWrapper;

  private final TaskExecutor taskExecutor;

  private final RedissonClient redissonClient;

  @Value("${megalith.blog.blog-page-size}")
  private int blogPageSize;

  public BlogWrapper(
      BlogHttpServiceWrapper blogHttpServiceWrapper,
      UserHttpServiceWrapper userHttpServiceWrapper,
      SearchHttpServiceWrapper searchHttpServiceWrapper,
      @Qualifier("commonExecutor") TaskExecutor taskExecutor,
      RedissonClient redissonClient) {
    this.blogHttpServiceWrapper = blogHttpServiceWrapper;
    this.userHttpServiceWrapper = userHttpServiceWrapper;
    this.searchHttpServiceWrapper = searchHttpServiceWrapper;
    this.taskExecutor = taskExecutor;
    this.redissonClient = redissonClient;
  }

  @Cache(
      namespace = BlogCacheDescriptors.DETAIL_NAMESPACE,
      version = BlogCacheDescriptors.VERSION)
  @Override
  public BlogExhibitDto findById(Long id) {
    BlogEntityRpcVo blogEntity = blogHttpServiceWrapper.findById(id);

    UserEntityRpcVo user = userHttpServiceWrapper.findById(blogEntity.userId());
    return BlogExhibitDtoConvertor.convert(blogEntity, user);
  }

  @Override
  public void incrementViews(Long id) {
    taskExecutor.execute(
        () -> {
          blogHttpServiceWrapper.setReadCount(id);
          redissonClient.<String>getScoredSortedSet(Const.HOT_READ).addScore(id.toString(), 1);
          searchHttpServiceWrapper.addReadCount(id);
        });
  }

  @Cache(
      namespace = BlogCacheDescriptors.PAGE_NAMESPACE,
      version = BlogCacheDescriptors.VERSION)
  @Override
  public PageAdapter<BlogDescriptionDto> findPage(Integer currentPage) {
    PageAdapter<BlogEntityRpcVo> page = blogHttpServiceWrapper.findPage(currentPage, blogPageSize);
    return BlogDescriptionDtoConvertor.convert(page);
  }
}
