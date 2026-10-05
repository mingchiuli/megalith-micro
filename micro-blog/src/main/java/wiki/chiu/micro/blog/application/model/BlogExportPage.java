package wiki.chiu.micro.blog.application.model;

import java.util.List;

import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.SensitiveContent;

/**
 * One page of the blog export: the blogs the caller may export and their sensitive-content rows.
 */
public record BlogExportPage(List<Blog> blogs, List<SensitiveContent> sensitiveContent) {
}
