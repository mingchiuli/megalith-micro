package wiki.chiu.micro.blog.service.impl;

import wiki.chiu.micro.blog.convertor.BlogSensitiveContentVoConvertor;
import wiki.chiu.micro.blog.entity.BlogSensitiveContentEntity;
import wiki.chiu.micro.blog.repository.BlogSensitiveContentRepository;
import wiki.chiu.micro.blog.service.BlogSensitiveService;
import wiki.chiu.micro.blog.vo.BlogSensitiveContentVo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogSensitiveServiceImpl implements BlogSensitiveService {

    private final BlogSensitiveContentRepository blogSensitiveContentRepository;

    public BlogSensitiveServiceImpl(BlogSensitiveContentRepository blogSensitiveContentRepository) {
        this.blogSensitiveContentRepository = blogSensitiveContentRepository;
    }

    @Override
    public BlogSensitiveContentVo findByBlogId(Long blogId) {
        List<BlogSensitiveContentEntity> entities = blogSensitiveContentRepository.findByBlogId(blogId);
        return BlogSensitiveContentVoConvertor.convert(entities);
    }

}
