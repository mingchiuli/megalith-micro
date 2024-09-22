package org.chiu.micro.search.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author mingchiuli
 * @create 2021-12-12 6:55 AM
 */
public class BlogDocumentVo {

    private Long id;

    private Long userId;

    private Integer status;

    private String title;

    private String description;

    private String content;

    private String link;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    private Float score;

    private Map<String, List<String>> highlight;

    BlogDocumentVo(Long id, Long userId, Integer status, String title, String description, String content, String link, LocalDateTime created, Float score, Map<String, List<String>> highlight) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.title = title;
        this.description = description;
        this.content = content;
        this.link = link;
        this.created = created;
        this.score = score;
        this.highlight = highlight;
    }

    public static BlogDocumentVoBuilder builder() {
        return new BlogDocumentVoBuilder();
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

    public LocalDateTime getCreated() {
        return this.created;
    }

    public Float getScore() {
        return this.score;
    }

    public Map<String, List<String>> getHighlight() {
        return this.highlight;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public void setHighlight(Map<String, List<String>> highlight) {
        this.highlight = highlight;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BlogDocumentVo)) return false;
        final BlogDocumentVo other = (BlogDocumentVo) o;
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
        final Object this$score = this.getScore();
        final Object other$score = other.getScore();
        if (this$score == null ? other$score != null : !this$score.equals(other$score)) return false;
        final Object this$highlight = this.getHighlight();
        final Object other$highlight = other.getHighlight();
        if (this$highlight == null ? other$highlight != null : !this$highlight.equals(other$highlight)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BlogDocumentVo;
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
        final Object $score = this.getScore();
        result = result * PRIME + ($score == null ? 43 : $score.hashCode());
        final Object $highlight = this.getHighlight();
        result = result * PRIME + ($highlight == null ? 43 : $highlight.hashCode());
        return result;
    }

    public String toString() {
        return "BlogDocumentVo(id=" + this.getId() + ", userId=" + this.getUserId() + ", status=" + this.getStatus() + ", title=" + this.getTitle() + ", description=" + this.getDescription() + ", content=" + this.getContent() + ", link=" + this.getLink() + ", created=" + this.getCreated() + ", score=" + this.getScore() + ", highlight=" + this.getHighlight() + ")";
    }

    public static class BlogDocumentVoBuilder {
        private Long id;
        private Long userId;
        private Integer status;
        private String title;
        private String description;
        private String content;
        private String link;
        private LocalDateTime created;
        private Float score;
        private Map<String, List<String>> highlight;

        BlogDocumentVoBuilder() {
        }

        public BlogDocumentVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogDocumentVoBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public BlogDocumentVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BlogDocumentVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogDocumentVoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlogDocumentVoBuilder content(String content) {
            this.content = content;
            return this;
        }

        public BlogDocumentVoBuilder link(String link) {
            this.link = link;
            return this;
        }

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        public BlogDocumentVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public BlogDocumentVoBuilder score(Float score) {
            this.score = score;
            return this;
        }

        public BlogDocumentVoBuilder highlight(Map<String, List<String>> highlight) {
            this.highlight = highlight;
            return this;
        }

        public BlogDocumentVo build() {
            return new BlogDocumentVo(this.id, this.userId, this.status, this.title, this.description, this.content, this.link, this.created, this.score, this.highlight);
        }

        public String toString() {
            return "BlogDocumentVo.BlogDocumentVoBuilder(id=" + this.id + ", userId=" + this.userId + ", status=" + this.status + ", title=" + this.title + ", description=" + this.description + ", content=" + this.content + ", link=" + this.link + ", created=" + this.created + ", score=" + this.score + ", highlight=" + this.highlight + ")";
        }
    }
}
