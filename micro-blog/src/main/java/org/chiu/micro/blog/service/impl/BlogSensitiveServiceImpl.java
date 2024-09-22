package org.chiu.micro.blog.service.impl;

import org.chiu.micro.blog.convertor.BlogSensitiveContentVoConvertor;
import org.chiu.micro.blog.entity.BlogSensitiveContentEntity;
import org.chiu.micro.blog.repository.BlogSensitiveContentRepository;
import org.chiu.micro.blog.service.BlogSensitiveService;
import org.chiu.micro.blog.vo.BlogSensitiveContentVo;
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
