package wiki.chiu.micro.blog.application.model;

import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;

public record BlogDraftDeleteCommand(
    BlogIdentity identity,
    BlogWorkingDraft draft,
    BlogVersion publishedRevision) {
}
