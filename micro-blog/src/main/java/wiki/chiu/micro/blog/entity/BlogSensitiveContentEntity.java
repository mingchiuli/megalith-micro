package wiki.chiu.micro.blog.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

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


    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlogSensitiveContentEntity that)) return false;

        return Objects.equals(id, that.id) && Objects.equals(blogId, that.blogId) && Objects.equals(startIndex, that.startIndex) && Objects.equals(endIndex, that.endIndex) && Objects.equals(type, that.type) && Objects.equals(created, that.created) && Objects.equals(updated, that.updated);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(blogId);
        result = 31 * result + Objects.hashCode(startIndex);
        result = 31 * result + Objects.hashCode(endIndex);
        result = 31 * result + Objects.hashCode(type);
        result = 31 * result + Objects.hashCode(created);
        result = 31 * result + Objects.hashCode(updated);
        return result;
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
    }
}
