package wiki.chiu.micro.blog.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

import org.hibernate.Hibernate;

import wiki.chiu.micro.common.constant.Const;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;

@Entity
@Table(
    name = Const.BLOG_REVISION_TABLE,
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_blog_revision_version",
            columnNames = {"blog_id", "version_no"}),
    indexes = @Index(name = "idx_blog_revision_blog", columnList = "blog_id,version_no"))
public class BlogRevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blog_id", nullable = false)
    private Long blogId;

    @Column(name = "version_no", nullable = false)
    private Long versionNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin", nullable = false, length = 24)
    private BlogRevisionOrigin origin;

    @Column(name = "base_revision_id")
    private Long baseRevisionId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "content", nullable = false, length = 65535)
    private String content;

    @Column(name = "link")
    private String link;

    @Enumerated(EnumType.STRING)
    @Column(name = "render_mode", nullable = false, length = 24)
    private BlogRenderMode renderMode;

    @Column(name = "first_published_at")
    private LocalDateTime firstPublishedAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected BlogRevisionEntity() {
    }

    public BlogRevisionEntity(
        Long id,
        Long blogId,
        Long versionNo,
        BlogRevisionOrigin origin,
        Long baseRevisionId,
        String title,
        String description,
        String content,
        String link,
        BlogRenderMode renderMode,
        LocalDateTime firstPublishedAt,
        Long createdBy,
        LocalDateTime createdAt) {
        this.id = id;
        this.blogId = blogId;
        this.versionNo = versionNo;
        this.origin = origin;
        this.baseRevisionId = baseRevisionId;
        this.title = title;
        this.description = description;
        this.content = content;
        this.link = link;
        this.renderMode = renderMode;
        this.firstPublishedAt = firstPublishedAt;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getBlogId() {
        return blogId;
    }

    public Long getVersionNo() {
        return versionNo;
    }

    public BlogRevisionOrigin getOrigin() {
        return origin;
    }

    public Long getBaseRevisionId() {
        return baseRevisionId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getContent() {
        return content;
    }

    public String getLink() {
        return link;
    }

    public BlogRenderMode getRenderMode() {
        return renderMode;
    }

    public LocalDateTime getFirstPublishedAt() {
        return firstPublishedAt;
    }

    public void setFirstPublishedAt(LocalDateTime firstPublishedAt) {
        this.firstPublishedAt = firstPublishedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public final boolean equals(Object value) {
        if (this == value) {
            return true;
        }
        if (value == null || Hibernate.getClass(this) != Hibernate.getClass(value)) {
            return false;
        }
        BlogRevisionEntity that = (BlogRevisionEntity) value;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
