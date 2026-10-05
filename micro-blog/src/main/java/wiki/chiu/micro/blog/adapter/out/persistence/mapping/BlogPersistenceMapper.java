package wiki.chiu.micro.blog.adapter.out.persistence.mapping;

import java.util.List;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogSensitiveContentEntity;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.SensitiveContent;

/**
 * Translates between the persisted entities and the framework-free domain model, so only this
 * adapter knows the JPA mapping.
 */
public final class BlogPersistenceMapper {

    private BlogPersistenceMapper() {
    }

    public static Blog toDomain(BlogEntity entity) {
        return new Blog(
            entity.getId(),
            entity.getUserId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getContent(),
            entity.getCreated(),
            entity.getUpdated(),
            entity.getStatus(),
            entity.getLink(),
            entity.getReadCount(),
            entity.getEventRevision());
    }

    public static List<Blog> toDomains(List<BlogEntity> entities) {
        return entities.stream().map(BlogPersistenceMapper::toDomain).toList();
    }

    public static BlogEntity toEntity(Blog blog) {
        return new BlogEntity(
            blog.id(),
            blog.userId(),
            blog.title(),
            blog.description(),
            blog.content(),
            blog.created(),
            blog.updated(),
            blog.status(),
            blog.link(),
            blog.readCount(),
            blog.eventRevision());
    }

    public static SensitiveContent toDomain(BlogSensitiveContentEntity entity) {
        return new SensitiveContent(
            entity.getId(),
            entity.getBlogId(),
            entity.getStartIndex(),
            entity.getEndIndex(),
            entity.getType(),
            entity.getCreated(),
            entity.getUpdated());
    }

    public static List<SensitiveContent> toSensitiveDomains(
        List<BlogSensitiveContentEntity> entities) {
        return entities.stream().map(BlogPersistenceMapper::toDomain).toList();
    }

    public static BlogSensitiveContentEntity toEntity(SensitiveContent content) {
        return new BlogSensitiveContentEntity(
            content.id(),
            content.blogId(),
            content.startIndex(),
            content.endIndex(),
            content.type(),
            content.created(),
            content.updated());
    }
}
