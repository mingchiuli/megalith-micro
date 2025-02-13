package wiki.chiu.micro.blog.service.impl;

import wiki.chiu.micro.blog.convertor.BlogSensitiveContentRpcVoConvertor;
import wiki.chiu.micro.blog.entity.BlogSensitiveContentEntity;
import wiki.chiu.micro.blog.repository.BlogSensitiveContentRepository;
import wiki.chiu.micro.blog.service.BlogSensitiveService;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.common.vo.BlogSensitiveContentRpcVo;

import java.util.List;

@Service
public class BlogSensitiveServiceImpl implements BlogSensitiveService {

    private final BlogSensitiveContentRepository blogSensitiveContentRepository;

    public BlogSensitiveServiceImpl(BlogSensitiveContentRepository blogSensitiveContentRepository) {
        this.blogSensitiveContentRepository = blogSensitiveContentRepository;
    }

    @Override
    public BlogSensitiveContentRpcVo findByBlogId(Long blogId) {
        List<BlogSensitiveContentEntity> entities = blogSensitiveContentRepository.findByBlogId(blogId);
        return BlogSensitiveContentRpcVoConvertor.convert(entities);
    }

}
