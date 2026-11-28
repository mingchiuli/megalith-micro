package wiki.chiu.micro.blog.application.model;

import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.BlogIdentity;

public record BlogDeletionTarget(Blog blog, BlogIdentity identity) {
}
