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

@Entity
@Table(
    name = Const.BLOG_DRAFT_TABLE,
    uniqueConstraints =
        @UniqueConstraint(name = "uk_blog_draft_blog", columnNames = "blog_id"),
    indexes = @Index(name = "idx_blog_draft_blog", columnList = "blog_id"))
public class BlogDraftEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blog_id", nullable = false)
    private Long blogId;

    @Column(name = "draft_revision", nullable = false)
    private Long draftRevision;

    @Column(name = "base_published_revision_id")
    private Long basePublishedRevisionId;

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

    @Column(name = "updated_by", nullable = false)
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected BlogDraftEntity() {
    }

    public BlogDraftEntity(
        Long id,
        Long blogId,
        Long draftRevision,
        Long basePublishedRevisionId,
        String title,
        String description,
        String content,
        String link,
        BlogRenderMode renderMode,
        Long updatedBy,
        LocalDateTime updatedAt) {
        this.id = id;
        this.blogId = blogId;
        this.draftRevision = draftRevision;
        this.basePublishedRevisionId = basePublishedRevisionId;
        this.title = title;
        this.description = description;
        this.content = content;
        this.link = link;
        this.renderMode = renderMode;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public static BlogDraftEntity create(
        Long blogId,
        Long basePublishedRevisionId,
        String title,
        String description,
        String content,
        String link,
        BlogRenderMode renderMode,
        Long updatedBy,
        LocalDateTime updatedAt) {
        return new BlogDraftEntity(
            null,
            blogId,
            1L,
            basePublishedRevisionId,
            title,
            description,
            content,
            link,
            renderMode,
            updatedBy,
            updatedAt);
    }

    public Long getId() {
        return id;
    }

    public Long getBlogId() {
        return blogId;
    }

    public Long getDraftRevision() {
        return draftRevision;
    }

    public void setDraftRevision(Long draftRevision) {
        this.draftRevision = draftRevision;
    }

    public Long getBasePublishedRevisionId() {
        return basePublishedRevisionId;
    }

    public void setBasePublishedRevisionId(Long basePublishedRevisionId) {
        this.basePublishedRevisionId = basePublishedRevisionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public BlogRenderMode getRenderMode() {
        return renderMode;
    }

    public void setRenderMode(BlogRenderMode renderMode) {
        this.renderMode = renderMode;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public final boolean equals(Object value) {
        if (this == value) {
            return true;
        }
        if (value == null || Hibernate.getClass(this) != Hibernate.getClass(value)) {
            return false;
        }
        BlogDraftEntity that = (BlogDraftEntity) value;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
