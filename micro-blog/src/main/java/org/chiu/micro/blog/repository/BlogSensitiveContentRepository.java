package org.chiu.micro.blog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.chiu.micro.blog.entity.BlogSensitiveContentEntity;

public interface BlogSensitiveContentRepository extends JpaRepository<BlogSensitiveContentEntity, Long> {

    List<BlogSensitiveContentEntity> findByBlogId(Long blogId);

    List<BlogSensitiveContentEntity> findByBlogIdIn(List<Long> ids);
}
