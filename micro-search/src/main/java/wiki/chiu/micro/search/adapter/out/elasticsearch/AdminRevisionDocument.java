package wiki.chiu.micro.search.adapter.out.elasticsearch;

import java.time.ZonedDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;

@Document(indexName = IndexConst.revisionIndexName, createIndex = false)
public class AdminRevisionDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long blogId;

    @Field(type = FieldType.Long)
    private Long versionNo;

    @Field(type = FieldType.Keyword)
    private BlogRevisionOrigin origin;

    @Field(type = FieldType.Long)
    private Long baseRevisionId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String description;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Keyword)
    private String link;

    @Field(type = FieldType.Keyword)
    private BlogRenderMode renderMode;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private ZonedDateTime firstPublishedAt;

    @Field(type = FieldType.Long)
    private Long createdBy;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private ZonedDateTime createdAt;

    protected AdminRevisionDocument() {
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

    public ZonedDateTime getFirstPublishedAt() {
        return firstPublishedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void apply(
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
        ZonedDateTime firstPublishedAt,
        Long createdBy,
        ZonedDateTime createdAt) {
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
}
