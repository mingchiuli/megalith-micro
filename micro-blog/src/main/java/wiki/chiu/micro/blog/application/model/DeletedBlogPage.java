package wiki.chiu.micro.blog.application.model;

import java.util.List;
import wiki.chiu.micro.blog.domain.BlogEntity;

public record DeletedBlogPage(int expiredCount, List<BlogEntity> blogs, long total) {}
