package wiki.chiu.micro.blog.application.model;

import wiki.chiu.micro.blog.domain.BlogEntity;

public record DeletedBlogEntry(BlogEntity blog, String receipt) {}
