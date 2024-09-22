package org.chiu.micro.blog.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name = "m_blog_sensitive_content",
        indexes = {
                @Index(columnList = "blog_id")})
public class BlogSensitiveContentEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blog_id")
    private Long blogId;

    @Column(name = "start_index")
    private Integer startIndex;

    @Column(name = "end_index")
    private Integer endIndex;

    @Column(name = "type")
    private Integer type;

    @Column(name = "created", updatable = false)
    @CreatedDate
    private LocalDateTime created;

    @Column(name = "updated")
    @LastModifiedDate
    private LocalDateTime updated;

    public BlogSensitiveContentEntity(Long id, Long blogId, Integer startIndex, Integer endIndex, Integer type, LocalDateTime created, LocalDateTime updated) {
        this.id = id;
        this.blogId = blogId;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.type = type;
        this.created = created;
        this.updated = updated;
    }

    public BlogSensitiveContentEntity() {
    }

    public static BlogSensitiveContentEntityBuilder builder() {
        return new BlogSensitiveContentEntityBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public Long getBlogId() {
        return this.blogId;
    }

    public Integer getStartIndex() {
        return this.startIndex;
    }

    public Integer getEndIndex() {
        return this.endIndex;
    }

    public Integer getType() {
        return this.type;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }

    public LocalDateTime getUpdated() {
        return this.updated;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    public void setEndIndex(Integer endIndex) {
        this.endIndex = endIndex;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BlogSensitiveContentEntity)) return false;
        final BlogSensitiveContentEntity other = (BlogSensitiveContentEntity) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$blogId = this.getBlogId();
        final Object other$blogId = other.getBlogId();
        if (this$blogId == null ? other$blogId != null : !this$blogId.equals(other$blogId)) return false;
        final Object this$startIndex = this.getStartIndex();
        final Object other$startIndex = other.getStartIndex();
        if (this$startIndex == null ? other$startIndex != null : !this$startIndex.equals(other$startIndex))
            return false;
        final Object this$endIndex = this.getEndIndex();
        final Object other$endIndex = other.getEndIndex();
        if (this$endIndex == null ? other$endIndex != null : !this$endIndex.equals(other$endIndex)) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        final Object this$created = this.getCreated();
        final Object other$created = other.getCreated();
        if (this$created == null ? other$created != null : !this$created.equals(other$created)) return false;
        final Object this$updated = this.getUpdated();
        final Object other$updated = other.getUpdated();
        if (this$updated == null ? other$updated != null : !this$updated.equals(other$updated)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BlogSensitiveContentEntity;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $blogId = this.getBlogId();
        result = result * PRIME + ($blogId == null ? 43 : $blogId.hashCode());
        final Object $startIndex = this.getStartIndex();
        result = result * PRIME + ($startIndex == null ? 43 : $startIndex.hashCode());
        final Object $endIndex = this.getEndIndex();
        result = result * PRIME + ($endIndex == null ? 43 : $endIndex.hashCode());
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final Object $created = this.getCreated();
        result = result * PRIME + ($created == null ? 43 : $created.hashCode());
        final Object $updated = this.getUpdated();
        result = result * PRIME + ($updated == null ? 43 : $updated.hashCode());
        return result;
    }

    public String toString() {
        return "BlogSensitiveContentEntity(id=" + this.getId() + ", blogId=" + this.getBlogId() + ", startIndex=" + this.getStartIndex() + ", endIndex=" + this.getEndIndex() + ", type=" + this.getType() + ", created=" + this.getCreated() + ", updated=" + this.getUpdated() + ")";
    }

    public static class BlogSensitiveContentEntityBuilder {
        private Long id;
        private Long blogId;
        private Integer startIndex;
        private Integer endIndex;
        private Integer type;
        private LocalDateTime created;
        private LocalDateTime updated;

        BlogSensitiveContentEntityBuilder() {
        }

        public BlogSensitiveContentEntityBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogSensitiveContentEntityBuilder blogId(Long blogId) {
            this.blogId = blogId;
            return this;
        }

        public BlogSensitiveContentEntityBuilder startIndex(Integer startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        public BlogSensitiveContentEntityBuilder endIndex(Integer endIndex) {
            this.endIndex = endIndex;
            return this;
        }

        public BlogSensitiveContentEntityBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public BlogSensitiveContentEntityBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public BlogSensitiveContentEntityBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public BlogSensitiveContentEntity build() {
            return new BlogSensitiveContentEntity(this.id, this.blogId, this.startIndex, this.endIndex, this.type, this.created, this.updated);
        }

        public String toString() {
            return "BlogSensitiveContentEntity.BlogSensitiveContentEntityBuilder(id=" + this.id + ", blogId=" + this.blogId + ", startIndex=" + this.startIndex + ", endIndex=" + this.endIndex + ", type=" + this.type + ", created=" + this.created + ", updated=" + this.updated + ")";
        }
    }
}
