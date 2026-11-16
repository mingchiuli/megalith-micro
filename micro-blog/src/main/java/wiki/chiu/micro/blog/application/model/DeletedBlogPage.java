package wiki.chiu.micro.blog.application.model;

import java.util.List;

public record DeletedBlogPage(List<DeletedBlogEntry> entries, long total) {
}
