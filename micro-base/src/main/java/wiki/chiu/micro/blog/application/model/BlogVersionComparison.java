package wiki.chiu.micro.blog.application.model;

import wiki.chiu.micro.blog.domain.BlogVersion;

public record BlogVersionComparison(BlogVersion previous, BlogVersion current) {
}
