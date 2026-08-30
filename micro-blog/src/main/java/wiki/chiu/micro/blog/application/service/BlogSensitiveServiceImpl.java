package wiki.chiu.micro.blog.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.blog.api.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.blog.application.port.in.BlogSensitiveService;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.convertor.BlogSensitiveContentRpcVoConvertor;
import wiki.chiu.micro.blog.domain.BlogSensitiveContentEntity;

@Service
public class BlogSensitiveServiceImpl implements BlogSensitiveService {

  private final BlogQueryStore blogs;

  public BlogSensitiveServiceImpl(BlogQueryStore blogs) {
    this.blogs = blogs;
  }

  @Override
  public BlogSensitiveContentRpcVo findByBlogId(Long blogId) {
    List<BlogSensitiveContentEntity> entities = blogs.findSensitiveByBlogId(blogId);
    return BlogSensitiveContentRpcVoConvertor.convert(entities);
  }
}
