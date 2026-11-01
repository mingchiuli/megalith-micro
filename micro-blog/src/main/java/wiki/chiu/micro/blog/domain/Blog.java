package wiki.chiu.micro.blog.domain;

import static wiki.chiu.micro.common.enums.BlogStatusEnum.NORMAL;

import java.time.LocalDateTime;
import java.util.List;

import wiki.chiu.micro.common.model.BlogSensitiveSpan;
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
    Long eventRevision,
    Long adminRevision,
    List<BlogSensitiveSpan> sensitiveContent) {

    public Blog(
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
        this(id, userId, title, description, content, created, updated, status, link, readCount,
            eventRevision, null, List.of());
    }

    public Blog(
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
        Long eventRevision,
        List<BlogSensitiveSpan> sensitiveContent) {
        this(
            id,
            userId,
            title,
            description,
            content,
            created,
            updated,
            status,
            link,
            readCount,
            eventRevision,
            null,
            sensitiveContent);
    }

    public Blog {
        sensitiveContent = sensitiveContent == null ? List.of() : List.copyOf(sensitiveContent);
    }

    public Blog withAdminRevision(Long revision) {
        return new Blog(
            id,
            userId,
            title,
            description,
            content,
            created,
            updated,
            status,
            link,
            readCount,
            eventRevision,
            revision,
            sensitiveContent);
    }

    /** A blank blog that belongs to the given author and has never been written. */
    public static Blog blankFor(Long userId) {
        return new Blog(null, userId, "", "", "", null, null, NORMAL.getCode(), "", 0L, null);
    }

    public Blog withEventRevision(Long revision) {
        return new Blog(
            id,
            userId,
            title,
            description,
            content,
            created,
            updated,
            status,
            link,
            readCount,
            revision,
            adminRevision,
            sensitiveContent);
    }

    /** Rebuilds a blog from its cross-service projection. */
    public static Blog fromSnapshot(BlogSnapshot snapshot) {
        return fromSnapshot(snapshot, null);
    }

    /** Rebuilds a blog and its administration revision from a cross-service projection. */
    public static Blog fromSnapshot(BlogSnapshot snapshot, Long adminRevision) {
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
            snapshot.revision(),
            adminRevision,
            snapshot.sensitiveContent());
    }

    /** The cross-service projection of this blog, published through the outbox. */
    public BlogSnapshot snapshot() {
        return new BlogSnapshot(
            id, userId, title, description, content, created, updated, status, link, readCount,
            eventRevision,
            null,
            null,
            null,
            null,
            null,
            sensitiveContent);
    }
}
