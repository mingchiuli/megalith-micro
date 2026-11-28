package wiki.chiu.micro.blog.application.model;

import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;

public record BlogEditorState(BlogIdentity identity, Blog published, BlogWorkingDraft draft) {
}
