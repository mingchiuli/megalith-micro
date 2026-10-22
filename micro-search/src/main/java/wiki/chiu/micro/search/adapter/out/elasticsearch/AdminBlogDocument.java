package wiki.chiu.micro.search.adapter.out.elasticsearch;

import java.time.ZonedDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import wiki.chiu.micro.common.enums.BlogRenderMode;

@Document(indexName = IndexConst.adminIndexName, createIndex = false)
public class AdminBlogDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Long)
    private Long publishedRevisionId;

    @Field(type = FieldType.Long)
    private Long publicationRevision;

    @Field(type = FieldType.Long)
    private Long adminRevision;

    @Field(type = FieldType.Long)
    private Long readCount;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private ZonedDateTime created;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private ZonedDateTime publishedAt;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private ZonedDateTime offlineAt;

    @Field(type = FieldType.Byte)
    private Integer accessMode;

    @Field(type = FieldType.Keyword)
    private BlogRenderMode renderMode;

    @Field(type = FieldType.Long)
    private Long draftRevision;

    @Field(type = FieldType.Long)
    private Long basePublishedRevisionId;

    @Field(type = FieldType.Boolean)
    private Boolean hasDraft;

    @Field(type = FieldType.Boolean)
    private Boolean draftNewer;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private ZonedDateTime draftUpdatedAt;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String publishedTitle;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String publishedDescription;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String publishedContent;

    @Field(type = FieldType.Keyword)
    private String publishedLink;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String draftTitle;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String draftDescription;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String draftContent;

    @Field(type = FieldType.Keyword)
    private String draftLink;

    @Field(type = FieldType.Byte)
    private Integer status;

    @Field(type = FieldType.Boolean)
    private Boolean deleted;

    protected AdminBlogDocument() {
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getPublishedRevisionId() {
        return publishedRevisionId;
    }

    public Long getPublicationRevision() {
        return publicationRevision;
    }

    public Long getAdminRevision() {
        return adminRevision;
    }

    public Long getReadCount() {
        return readCount;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public ZonedDateTime getPublishedAt() {
        return publishedAt;
    }

    public ZonedDateTime getOfflineAt() {
        return offlineAt;
    }

    public Integer getAccessMode() {
        return accessMode;
    }

    public BlogRenderMode getRenderMode() {
        return renderMode;
    }

    public Long getDraftRevision() {
        return draftRevision;
    }

    public Long getBasePublishedRevisionId() {
        return basePublishedRevisionId;
    }

    public Boolean getHasDraft() {
        return hasDraft;
    }

    public Boolean getDraftNewer() {
        return draftNewer;
    }

    public ZonedDateTime getDraftUpdatedAt() {
        return draftUpdatedAt;
    }

    public String getPublishedTitle() {
        return publishedTitle;
    }

    public String getPublishedDescription() {
        return publishedDescription;
    }

    public String getPublishedContent() {
        return publishedContent;
    }

    public String getPublishedLink() {
        return publishedLink;
    }

    public String getDraftTitle() {
        return draftTitle;
    }

    public String getDraftDescription() {
        return draftDescription;
    }

    public String getDraftContent() {
        return draftContent;
    }

    public String getDraftLink() {
        return draftLink;
    }

    public Integer getStatus() {
        return status;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void apply(AdminBlogProjection source) {
        id = source.id();
        userId = source.userId();
        publishedRevisionId = source.publishedRevisionId();
        publicationRevision = source.publicationRevision();
        adminRevision = source.adminRevision();
        readCount = source.readCount();
        created = source.created();
        publishedAt = source.publishedAt();
        offlineAt = source.offlineAt();
        accessMode = source.accessMode();
        renderMode = source.renderMode();
        draftRevision = source.draftRevision();
        basePublishedRevisionId = source.basePublishedRevisionId();
        hasDraft = source.hasDraft();
        draftNewer = source.draftNewer();
        draftUpdatedAt = source.draftUpdatedAt();
        publishedTitle = source.publishedTitle();
        publishedDescription = source.publishedDescription();
        publishedContent = source.publishedContent();
        publishedLink = source.publishedLink();
        draftTitle = source.draftTitle();
        draftDescription = source.draftDescription();
        draftContent = source.draftContent();
        draftLink = source.draftLink();
        status = source.status();
        deleted = source.deleted();
    }
}
