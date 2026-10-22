package wiki.chiu.micro.blog.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import org.hibernate.Hibernate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import wiki.chiu.micro.common.constant.Const;
import wiki.chiu.micro.common.enums.BlogAccessMode;

/**
 * Stable blog identity. Content is stored only in revisions or the mutable working draft.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = Const.BLOG_TABLE,
    indexes = {
        @Index(name = "idx_blog_user", columnList = "user_id"),
        @Index(name = "idx_blog_published", columnList = "published_revision_id")
    })
public class BlogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime created;

    @Column(name = "read_count", nullable = false)
    private Long readCount;

    @Column(name = "published_revision_id")
    private Long publishedRevisionId;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "offline_at")
    private LocalDateTime offlineAt;

    @Column(name = "access_mode")
    private Integer accessMode;

    @Column(name = "publication_revision")
    private Long publicationRevision;

    @Column(name = "admin_revision")
    private Long adminRevision;

    protected BlogEntity() {
    }

    public BlogEntity(
        Long id,
        Long userId,
        LocalDateTime created,
        Long readCount,
        Long publishedRevisionId,
        LocalDateTime publishedAt,
        LocalDateTime offlineAt,
        Integer accessMode,
        Long publicationRevision,
        Long adminRevision) {
        this.id = id;
        this.userId = userId;
        this.created = created;
        this.readCount = readCount;
        this.publishedRevisionId = publishedRevisionId;
        this.publishedAt = publishedAt;
        this.offlineAt = offlineAt;
        this.accessMode = accessMode;
        this.publicationRevision = publicationRevision;
        this.adminRevision = adminRevision;
    }

    public static BlogEntity newBlog(Long userId) {
        return new BlogEntity(
            null,
            userId,
            null,
            0L,
            null,
            null,
            null,
            BlogAccessMode.PUBLIC.getCode(),
            0L,
            0L);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public Long getReadCount() {
        return readCount;
    }

    public void setReadCount(Long readCount) {
        this.readCount = readCount;
    }

    public Long getPublishedRevisionId() {
        return publishedRevisionId;
    }

    public void setPublishedRevisionId(Long publishedRevisionId) {
        this.publishedRevisionId = publishedRevisionId;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getOfflineAt() {
        return offlineAt;
    }

    public void setOfflineAt(LocalDateTime offlineAt) {
        this.offlineAt = offlineAt;
    }

    public Integer getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(Integer accessMode) {
        this.accessMode = accessMode;
    }

    public Long getPublicationRevision() {
        return publicationRevision;
    }

    public void setPublicationRevision(Long publicationRevision) {
        this.publicationRevision = publicationRevision;
    }

    public Long getAdminRevision() {
        return adminRevision;
    }

    public void setAdminRevision(Long adminRevision) {
        this.adminRevision = adminRevision;
    }

    @Override
    public final boolean equals(Object value) {
        if (this == value) {
            return true;
        }
        if (value == null || Hibernate.getClass(this) != Hibernate.getClass(value)) {
            return false;
        }
        BlogEntity that = (BlogEntity) value;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
