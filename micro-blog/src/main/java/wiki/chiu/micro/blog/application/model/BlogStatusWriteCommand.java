package wiki.chiu.micro.blog.application.model;

import java.time.LocalDateTime;

import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;

public record BlogStatusWriteCommand(
    BlogIdentity identity,
    BlogVersion revision,
    BlogWorkingDraft draft,
    Long userId,
    LocalDateTime now,
    boolean online) {
}
