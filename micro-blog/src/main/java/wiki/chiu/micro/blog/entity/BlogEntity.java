package wiki.chiu.micro.blog.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author mingchiuli
 * @create 2022-11-27 12:56 am
 */
@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name = "m_blog",
        indexes = {@Index(columnList = "created")})
public class BlogEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "content", length = 65535)
    private String content;

    @Column(name = "created", updatable = false)
    @CreatedDate
    private LocalDateTime created;

    @Column(name = "updated")
    @LastModifiedDate
    private LocalDateTime updated;

    @Column(name = "status")
    private Integer status;

    @Column(name = "link")
    private String link;

    @Column(name = "read_count")
    private Long readCount;

    public BlogEntity(Long id, Long userId, String title, String description, String content, LocalDateTime created, LocalDateTime updated, Integer status, String link, Long readCount) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.content = content;
        this.created = created;
        this.updated = updated;
        this.status = status;
        this.link = link;
        this.readCount = readCount;
    }

    public BlogEntity() {
    }

    public static BlogEntityBuilder builder() {
        return new BlogEntityBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getContent() {
        return this.content;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }

    public LocalDateTime getUpdated() {
        return this.updated;
    }

    public Integer getStatus() {
        return this.status;
    }

    public String getLink() {
        return this.link;
    }

    public Long getReadCount() {
        return this.readCount;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setReadCount(Long readCount) {
        this.readCount = readCount;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlogEntity that)) return false;

        return Objects.equals(id, that.id) && Objects.equals(userId, that.userId) && Objects.equals(title, that.title) && Objects.equals(description, that.description) && Objects.equals(content, that.content) && Objects.equals(created, that.created) && Objects.equals(updated, that.updated) && Objects.equals(status, that.status) && Objects.equals(link, that.link) && Objects.equals(readCount, that.readCount);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(userId);
        result = 31 * result + Objects.hashCode(title);
        result = 31 * result + Objects.hashCode(description);
        result = 31 * result + Objects.hashCode(content);
        result = 31 * result + Objects.hashCode(created);
        result = 31 * result + Objects.hashCode(updated);
        result = 31 * result + Objects.hashCode(status);
        result = 31 * result + Objects.hashCode(link);
        result = 31 * result + Objects.hashCode(readCount);
        return result;
    }

    public static class BlogEntityBuilder {
        private Long id;
        private Long userId;
        private String title;
        private String description;
        private String content;
        private LocalDateTime created;
        private LocalDateTime updated;
        private Integer status;
        private String link;
        private Long readCount;


        public BlogEntityBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogEntityBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public BlogEntityBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogEntityBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlogEntityBuilder content(String content) {
            this.content = content;
            return this;
        }

        public BlogEntityBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public BlogEntityBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public BlogEntityBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BlogEntityBuilder link(String link) {
            this.link = link;
            return this;
        }

        public BlogEntityBuilder readCount(Long readCount) {
            this.readCount = readCount;
            return this;
        }

        public BlogEntity build() {
            return new BlogEntity(this.id, this.userId, this.title, this.description, this.content, this.created, this.updated, this.status, this.link, this.readCount);
        }

    }
}
