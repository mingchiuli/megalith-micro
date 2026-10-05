package wiki.chiu.micro.blog.application.model;

import wiki.chiu.micro.blog.domain.Blog;

public record DeletedBlogEntry(Blog blog, String receipt) {
}
