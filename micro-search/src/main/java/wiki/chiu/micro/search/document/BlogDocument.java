package wiki.chiu.micro.search.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.ZonedDateTime;
import java.util.Objects;

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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlogDocument that)) return false;

        return Objects.equals(id, that.id) && Objects.equals(userId, that.userId) && Objects.equals(status, that.status) && Objects.equals(title, that.title) && Objects.equals(description, that.description) && Objects.equals(content, that.content) && Objects.equals(link, that.link) && Objects.equals(created, that.created) && Objects.equals(updated, that.updated);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(userId);
        result = 31 * result + Objects.hashCode(status);
        result = 31 * result + Objects.hashCode(title);
        result = 31 * result + Objects.hashCode(description);
        result = 31 * result + Objects.hashCode(content);
        result = 31 * result + Objects.hashCode(link);
        result = 31 * result + Objects.hashCode(created);
        result = 31 * result + Objects.hashCode(updated);
        return result;
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
            return new BlogDocument(id, userId, status, title, description, content, link, created, updated);
        }
    }
}
