package wiki.chiu.micro.blog.application.model;

import java.util.List;

import wiki.chiu.micro.blog.domain.Blog;

public record DeletedBlogPage(int expiredCount, List<Blog> blogs, long total) {
}
