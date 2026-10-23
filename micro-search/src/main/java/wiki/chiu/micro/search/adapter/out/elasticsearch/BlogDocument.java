package wiki.chiu.micro.search.adapter.out.elasticsearch;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;

/**
 * @author mingchiuli
 * @create 2022-11-30 8:55 pm
 */
@Document(indexName = IndexConst.indexName, createIndex = false)
public class BlogDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Byte)
    private Integer status;

    @Field(type = FieldType.Long)
    private Long readCount;

    @Field(type = FieldType.Long)
    private Long revision;

    @Field(type = FieldType.Long)
    private Long publishedRevisionId;

    @Field(type = FieldType.Long)
    private Long versionNo;

    @Field(type = FieldType.Byte)
    private Integer accessMode;

    @Field(type = FieldType.Keyword)
    private BlogRenderMode renderMode;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private ZonedDateTime publishedAt;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private ZonedDateTime offlineAt;

    @Field(type = FieldType.Nested)
    private List<BlogSensitiveSpan> sensitiveContent;

    @Field(type = FieldType.Boolean)
    private Boolean deleted;

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

    public BlogDocument() {
    }

    BlogDocument(
        Long id,
        Long userId,
        Integer status,
        Long readCount,
        String title,
        String description,
        String content,
        String link,
        ZonedDateTime created,
        ZonedDateTime updated,
        Long revision,
        Boolean deleted,
        Long publishedRevisionId,
        Long versionNo,
        Integer accessMode,
        BlogRenderMode renderMode,
        ZonedDateTime publishedAt,
        ZonedDateTime offlineAt,
        List<BlogSensitiveSpan> sensitiveContent) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.readCount = readCount;
        this.title = title;
        this.description = description;
        this.content = content;
        this.link = link;
        this.created = created;
        this.updated = updated;
        this.revision = revision;
        this.deleted = deleted;
        this.publishedRevisionId = publishedRevisionId;
        this.versionNo = versionNo;
        this.accessMode = accessMode;
        this.renderMode = renderMode;
        this.publishedAt = publishedAt;
        this.offlineAt = offlineAt;
        this.sensitiveContent = sensitiveContent;
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
        return link;
    }

    public Long getReadCount() {
        return readCount;
    }

    public void setReadCount(Long readCount) {
        this.readCount = readCount;
    }

    public ZonedDateTime getCreated() {
        return this.created;
    }

    public ZonedDateTime getUpdated() {
        return this.updated;
    }

    public Long getRevision() {
        return revision;
    }

    public Long getPublishedRevisionId() {
        return publishedRevisionId;
    }

    public Long getVersionNo() {
        return versionNo;
    }

    public Integer getAccessMode() {
        return accessMode;
    }

    public BlogRenderMode getRenderMode() {
        return renderMode;
    }

    public ZonedDateTime getPublishedAt() {
        return publishedAt;
    }

    public ZonedDateTime getOfflineAt() {
        return offlineAt;
    }

    public List<BlogSensitiveSpan> getSensitiveContent() {
        return sensitiveContent;
    }

    public Boolean getDeleted() {
        return deleted;
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

        return Objects.equals(id, that.id)
            && Objects.equals(userId, that.userId)
            && Objects.equals(status, that.status)
            && Objects.equals(readCount, that.readCount)
            && Objects.equals(title, that.title)
            && Objects.equals(description, that.description)
            && Objects.equals(content, that.content)
            && Objects.equals(created, that.created)
            && Objects.equals(updated, that.updated);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(userId);
        result = 31 * result + Objects.hashCode(status);
        result = 31 * result + Objects.hashCode(readCount);
        result = 31 * result + Objects.hashCode(title);
        result = 31 * result + Objects.hashCode(description);
        result = 31 * result + Objects.hashCode(content);
        result = 31 * result + Objects.hashCode(created);
        result = 31 * result + Objects.hashCode(updated);
        return result;
    }

    public static class BlogDocumentBuilder {
        private Long id;
        private Long userId;
        private Integer status;
        private Long readCount;
        private String title;
        private String description;
        private String content;
        private String link;
        private ZonedDateTime created;
        private ZonedDateTime updated;
        private Long revision;
        private Boolean deleted;
        private Long publishedRevisionId;
        private Long versionNo;
        private Integer accessMode;
        private BlogRenderMode renderMode;
        private ZonedDateTime publishedAt;
        private ZonedDateTime offlineAt;
        private List<BlogSensitiveSpan> sensitiveContent;

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

        public BlogDocumentBuilder readCount(Long readCount) {
            this.readCount = readCount;
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

        public BlogDocumentBuilder revision(Long revision) {
            this.revision = revision;
            return this;
        }

        public BlogDocumentBuilder deleted(Boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public BlogDocumentBuilder publishedRevisionId(Long publishedRevisionId) {
            this.publishedRevisionId = publishedRevisionId;
            return this;
        }

        public BlogDocumentBuilder versionNo(Long versionNo) {
            this.versionNo = versionNo;
            return this;
        }

        public BlogDocumentBuilder accessMode(Integer accessMode) {
            this.accessMode = accessMode;
            return this;
        }

        public BlogDocumentBuilder renderMode(BlogRenderMode renderMode) {
            this.renderMode = renderMode;
            return this;
        }

        public BlogDocumentBuilder publishedAt(ZonedDateTime publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }

        public BlogDocumentBuilder offlineAt(ZonedDateTime offlineAt) {
            this.offlineAt = offlineAt;
            return this;
        }

        public BlogDocumentBuilder sensitiveContent(List<BlogSensitiveSpan> sensitiveContent) {
            this.sensitiveContent = sensitiveContent;
            return this;
        }

        public BlogDocument build() {
            return new BlogDocument(
                id,
                userId,
                status,
                readCount,
                title,
                description,
                content,
                link,
                created,
                updated,
                revision,
                deleted,
                publishedRevisionId,
                versionNo,
                accessMode,
                renderMode,
                publishedAt,
                offlineAt,
                sensitiveContent);
        }
    }
}
