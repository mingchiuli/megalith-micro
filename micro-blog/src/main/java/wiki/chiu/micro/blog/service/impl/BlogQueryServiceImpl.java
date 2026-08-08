package wiki.chiu.micro.blog.service.impl;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

import java.time.LocalDateTime;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.blog.convertor.BlogEntityRpcVoConvertor;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.repository.BlogRepository;
import wiki.chiu.micro.blog.service.BlogQueryService;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.BlogStatusEnum;
import wiki.chiu.micro.common.page.PageAdapter;

@Service
public class BlogQueryServiceImpl implements BlogQueryService {

  private final BlogRepository blogs;

  public BlogQueryServiceImpl(BlogRepository blogs) {
    this.blogs = blogs;
  }

  @Override
  public BlogEntityRpcVo findById(Long blogId) {
    BlogEntity blog =
        blogs.findById(blogId).orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
    return BlogEntityRpcVoConvertor.convert(blog);
  }

  @Override
  public List<BlogEntityRpcVo> findAllById(List<Long> ids) {
    return BlogEntityRpcVoConvertor.convert(blogs.findAllById(ids));
  }

  @Override
  public long count() {
    return blogs.count();
  }

  @Override
  public void incrementViews(Long blogId) {
    blogs.setReadCount(blogId);
  }

  @Override
  public PageAdapter<BlogEntityRpcVo> findPage(Integer pageNo, Integer pageSize) {
    var pageRequest = PageRequest.of(pageNo - 1, pageSize, Sort.by("created").descending());
    List<Integer> statuses =
        List.of(
            BlogStatusEnum.NORMAL.getCode(),
            BlogStatusEnum.SENSITIVE_FILTER.getCode(),
            BlogStatusEnum.HIDE.getCode());
    Page<@NonNull BlogEntity> page = blogs.findByStatusIn(pageRequest, statuses);
    return BlogEntityRpcVoConvertor.convert(page);
  }

  @Override
  public long countCreatedSince(LocalDateTime created) {
    return blogs.countByCreatedGreaterThanEqual(created);
  }
}
