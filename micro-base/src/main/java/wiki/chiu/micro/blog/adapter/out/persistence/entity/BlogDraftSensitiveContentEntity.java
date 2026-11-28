package wiki.chiu.micro.blog.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import wiki.chiu.micro.common.constant.Const;

@Entity
@Table(
    name = Const.BLOG_DRAFT_SENSITIVE_TABLE,
    indexes = @Index(name = "idx_blog_draft_span", columnList = "blog_id"))
public class BlogDraftSensitiveContentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blog_id", nullable = false)
    private Long blogId;

    @Column(name = "start_index", nullable = false)
    private Integer startIndex;

    @Column(name = "end_index", nullable = false)
    private Integer endIndex;

    @Column(name = "type", nullable = false)
    private Integer type;

    protected BlogDraftSensitiveContentEntity() {
    }

    public BlogDraftSensitiveContentEntity(
        Long id, Long blogId, Integer startIndex, Integer endIndex, Integer type) {
        this.id = id;
        this.blogId = blogId;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public Long getBlogId() {
        return blogId;
    }

    public Integer getStartIndex() {
        return startIndex;
    }

    public Integer getEndIndex() {
        return endIndex;
    }

    public Integer getType() {
        return type;
    }
}
