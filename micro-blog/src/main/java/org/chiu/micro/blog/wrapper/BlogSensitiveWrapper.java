package org.chiu.micro.blog.wrapper;

import org.chiu.micro.blog.entity.BlogEntity;
import org.chiu.micro.blog.entity.BlogSensitiveContentEntity;
import org.chiu.micro.blog.repository.BlogRepository;
import org.chiu.micro.blog.repository.BlogSensitiveContentRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class BlogSensitiveWrapper {

    private final BlogRepository blogRepository;

    private final BlogSensitiveContentRepository blogSensitiveContentRepository;

    public BlogSensitiveWrapper(BlogRepository blogRepository, BlogSensitiveContentRepository blogSensitiveContentRepository) {
        this.blogRepository = blogRepository;
        this.blogSensitiveContentRepository = blogSensitiveContentRepository;
    }

    @Transactional
    public BlogEntity saveOrUpdate(BlogEntity blog, List<BlogSensitiveContentEntity> blogSensitiveContentEntityList, List<Long> existedSensitiveIds) {
        BlogEntity savedBlogEntity = blogRepository.save(blog);
        Long blogId = savedBlogEntity.getId();

        if (!existedSensitiveIds.isEmpty()) {
            blogSensitiveContentRepository.deleteAllById(existedSensitiveIds);
        }
        if (!blogSensitiveContentEntityList.isEmpty()) {
            blogSensitiveContentEntityList.forEach(item -> item.setBlogId(blogId));
            blogSensitiveContentRepository.saveAll(blogSensitiveContentEntityList);
        }
        return savedBlogEntity;
    }

    @Transactional
    public void deleteByIds(List<Long> ids, List<Long> sensitiveIds) {
        blogRepository.deleteAllById(ids);
        if (!sensitiveIds.isEmpty()) {
            blogSensitiveContentRepository.deleteAllById(sensitiveIds);
        }
    }
}
