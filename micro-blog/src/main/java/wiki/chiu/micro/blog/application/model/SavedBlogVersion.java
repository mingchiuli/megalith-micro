package wiki.chiu.micro.blog.application.model;

import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;

public record SavedBlogVersion(
    BlogWorkingDraft draft,
    BlogVersion version) {
}
