package org.chiu.micro.blog.wrapper;

import java.util.List;

import org.chiu.micro.blog.entity.BlogEntity;
import org.chiu.micro.blog.entity.BlogSensitiveContentEntity;
import org.chiu.micro.blog.repository.BlogRepository;
import org.chiu.micro.blog.repository.BlogSensitiveContentRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BlogSensitiveWrapper {

    private final BlogRepository blogRepository;

    private final BlogSensitiveContentRepository blogSensitiveContentRepository;

    @Transactional
    public BlogEntity saveOrUpdate(BlogEntity blog, List<BlogSensitiveContentEntity> blogSensitiveContentEntityList, List<Long> existedSensitiveIds) {
        BlogEntity savedBlogEntity = blogRepository.save(blog);

        if (!existedSensitiveIds.isEmpty()) {
            blogSensitiveContentRepository.deleteAllById(existedSensitiveIds);
            blogSensitiveContentRepository.flush();
        }
        if (!blogSensitiveContentEntityList.isEmpty()) {
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
