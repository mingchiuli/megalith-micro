package wiki.chiu.micro.blog.application.model;

import java.time.LocalDateTime;

import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;

public record BlogDraftWriteCommand(
    BlogIdentity identity,
    BlogWorkingDraft currentDraft,
    BlogContentCommand content,
    BlogVersion publishedRevision,
    Long userId,
    LocalDateTime now) {
}
