package wiki.chiu.micro.blog.domain;

import static wiki.chiu.micro.common.enums.BlogStatusEnum.NORMAL;

import java.time.LocalDateTime;

import wiki.chiu.micro.common.model.BlogSnapshot;

/**
 * A blog as the application knows it: the persisted state without any persistence, delivery, or
 * messaging dependency.
 */
public record Blog(
    Long id,
    Long userId,
    String title,
    String description,
    String content,
    LocalDateTime created,
    LocalDateTime updated,
    Integer status,
    String link,
    Long readCount,
    Long eventRevision) {

    /** A blank blog that belongs to the given author and has never been written. */
    public static Blog blankFor(Long userId) {
        return new Blog(null, userId, "", "", "", null, null, NORMAL.getCode(), "", 0L, null);
    }

    /** A blog placeholder that carries only the author and a fresh read counter. */
    public static Blog newBy(Long userId) {
        return new Blog(null, userId, null, null, null, null, null, null, null, 0L, null);
    }

    public Blog withEventRevision(Long revision) {
        return new Blog(
            id, userId, title, description, content, created, updated, status, link, readCount, revision);
    }

    public Blog withUpdated(LocalDateTime newUpdated) {
        return new Blog(
            id, userId, title, description, content, created, newUpdated, status, link, readCount,
            eventRevision);
    }

    /** Rebuilds a blog from its cross-service projection. */
    public static Blog fromSnapshot(BlogSnapshot snapshot) {
        return new Blog(
            snapshot.id(),
            snapshot.userId(),
            snapshot.title(),
            snapshot.description(),
            snapshot.content(),
            snapshot.created(),
            snapshot.updated(),
            snapshot.status(),
            snapshot.link(),
            snapshot.readCount(),
            snapshot.revision());
    }

    /** The cross-service projection of this blog, published through the outbox. */
    public BlogSnapshot snapshot() {
        return new BlogSnapshot(
            id, userId, title, description, content, created, updated, status, link, readCount,
            eventRevision);
    }
}
