package org.chiu.micro.search.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.ZonedDateTime;

/**
 * @author mingchiuli
 * @create 2022-11-30 8:55 pm
 */
@Document(indexName = "blog_index_v2")
public class BlogDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Byte)
    private Integer status;

    @Field(type = FieldType.Text, searchAnalyzer = "ik_smart", analyzer = "ik_max_word")
    private String title;

    @Field(type = FieldType.Text, searchAnalyzer = "ik_smart", analyzer = "ik_max_word")
    private String description;

    @Field(type = FieldType.Text, searchAnalyzer = "ik_smart", analyzer = "ik_max_word")
    private String content;

    @Field(type = FieldType.Keyword)
    private String link;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private ZonedDateTime created;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private ZonedDateTime updated;

    BlogDocument(Long id, Long userId, Integer status, String title, String description, String content, String link, ZonedDateTime created, ZonedDateTime updated) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.title = title;
        this.description = description;
        this.content = content;
        this.link = link;
        this.created = created;
        this.updated = updated;
    }

    public static BlogDocumentBuilder builder() {
        return new BlogDocumentBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public Integer getStatus() {
        return this.status;
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

    public String getLink() {
        return this.link;
    }

    public ZonedDateTime getCreated() {
        return this.created;
    }

    public ZonedDateTime getUpdated() {
        return this.updated;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public void setLink(String link) {
        this.link = link;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }

    public void setUpdated(ZonedDateTime updated) {
        this.updated = updated;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BlogDocument)) return false;
        final BlogDocument other = (BlogDocument) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$userId = this.getUserId();
        final Object other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        if (this$title == null ? other$title != null : !this$title.equals(other$title)) return false;
        final Object this$description = this.getDescription();
        final Object other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description))
            return false;
        final Object this$content = this.getContent();
        final Object other$content = other.getContent();
        if (this$content == null ? other$content != null : !this$content.equals(other$content)) return false;
        final Object this$link = this.getLink();
        final Object other$link = other.getLink();
        if (this$link == null ? other$link != null : !this$link.equals(other$link)) return false;
        final Object this$created = this.getCreated();
        final Object other$created = other.getCreated();
        if (this$created == null ? other$created != null : !this$created.equals(other$created)) return false;
        final Object this$updated = this.getUpdated();
        final Object other$updated = other.getUpdated();
        if (this$updated == null ? other$updated != null : !this$updated.equals(other$updated)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BlogDocument;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $userId = this.getUserId();
        result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        final Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        final Object $content = this.getContent();
        result = result * PRIME + ($content == null ? 43 : $content.hashCode());
        final Object $link = this.getLink();
        result = result * PRIME + ($link == null ? 43 : $link.hashCode());
        final Object $created = this.getCreated();
        result = result * PRIME + ($created == null ? 43 : $created.hashCode());
        final Object $updated = this.getUpdated();
        result = result * PRIME + ($updated == null ? 43 : $updated.hashCode());
        return result;
    }

    public String toString() {
        return "BlogDocument(id=" + this.getId() + ", userId=" + this.getUserId() + ", status=" + this.getStatus() + ", title=" + this.getTitle() + ", description=" + this.getDescription() + ", content=" + this.getContent() + ", link=" + this.getLink() + ", created=" + this.getCreated() + ", updated=" + this.getUpdated() + ")";
    }

    public static class BlogDocumentBuilder {
        private Long id;
        private Long userId;
        private Integer status;
        private String title;
        private String description;
        private String content;
        private String link;
        private ZonedDateTime created;
        private ZonedDateTime updated;

        BlogDocumentBuilder() {
        }

        public BlogDocumentBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogDocumentBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public BlogDocumentBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BlogDocumentBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogDocumentBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlogDocumentBuilder content(String content) {
            this.content = content;
            return this;
        }

        public BlogDocumentBuilder link(String link) {
            this.link = link;
            return this;
        }

        public BlogDocumentBuilder created(ZonedDateTime created) {
            this.created = created;
            return this;
        }

        public BlogDocumentBuilder updated(ZonedDateTime updated) {
            this.updated = updated;
            return this;
        }

        public BlogDocument build() {
            return new BlogDocument(this.id, this.userId, this.status, this.title, this.description, this.content, this.link, this.created, this.updated);
        }

        public String toString() {
            return "BlogDocument.BlogDocumentBuilder(id=" + this.id + ", userId=" + this.userId + ", status=" + this.status + ", title=" + this.title + ", description=" + this.description + ", content=" + this.content + ", link=" + this.link + ", created=" + this.created + ", updated=" + this.updated + ")";
        }
    }
}
