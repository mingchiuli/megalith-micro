package wiki.chiu.micro.blog.application.model;

import java.util.List;

import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.SensitiveContent;

/**
 * The blog a client submits, before it is checked against the stored state.
 */
public record BlogDraft(
    Long id,
    String title,
    String description,
    String content,
    Integer status,
    String link,
    List<SensitiveContentDraft> sensitiveContentList) {

    public boolean isNew() {
        return id == null;
    }

    /**
     * Merges this draft over the state that must be preserved: the author, the first creation time,
     * the accumulated read count, and the next event revision.
     */
    public Blog mergeInto(Blog current) {
        return new Blog(
            id,
            current.userId(),
            title,
            description,
            content,
            current.created(),
            current.updated(),
            status,
            link,
            current.readCount(),
            current.eventRevision() == null ? 1L : current.eventRevision() + 1);
    }

    public List<SensitiveContent> sensitiveContents() {
        return sensitiveContentList.stream()
            .map(item -> new SensitiveContent(
                    null, null, item.startIndex(), item.endIndex(), item.type(), null, null))
            .toList();
    }
}
