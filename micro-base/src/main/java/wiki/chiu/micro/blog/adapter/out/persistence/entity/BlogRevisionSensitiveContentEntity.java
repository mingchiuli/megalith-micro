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
    name = Const.BLOG_REVISION_SENSITIVE_TABLE,
    indexes = @Index(name = "idx_blog_revision_span", columnList = "revision_id"))
public class BlogRevisionSensitiveContentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "revision_id", nullable = false)
    private Long revisionId;

    @Column(name = "start_index", nullable = false)
    private Integer startIndex;

    @Column(name = "end_index", nullable = false)
    private Integer endIndex;

    @Column(name = "type", nullable = false)
    private Integer type;

    protected BlogRevisionSensitiveContentEntity() {
    }

    public BlogRevisionSensitiveContentEntity(
        Long id, Long revisionId, Integer startIndex, Integer endIndex, Integer type) {
        this.id = id;
        this.revisionId = revisionId;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public Long getRevisionId() {
        return revisionId;
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
