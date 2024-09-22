package org.chiu.micro.blog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * @author mingchiuli
 * @create 2022-12-03 11:36 pm
 */
public class BlogEntityVo {

    private Long id;

    private String title;

    private Boolean owner;

    private String description;

    private String content;

    private String link;

    private Long readCount;

    private Integer recentReadCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated;

    private Integer status;

    BlogEntityVo(Long id, String title, Boolean owner, String description, String content, String link, Long readCount, Integer recentReadCount, LocalDateTime created, LocalDateTime updated, Integer status) {
        this.id = id;
        this.title = title;
        this.owner = owner;
        this.description = description;
        this.content = content;
        this.link = link;
        this.readCount = readCount;
        this.recentReadCount = recentReadCount;
        this.created = created;
        this.updated = updated;
        this.status = status;
    }

    public static BlogEntityVoBuilder builder() {
        return new BlogEntityVoBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public Boolean getOwner() {
        return this.owner;
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

    public Long getReadCount() {
        return this.readCount;
    }

    public Integer getRecentReadCount() {
        return this.recentReadCount;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOwner(Boolean owner) {
        this.owner = owner;
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

    public void setReadCount(Long readCount) {
        this.readCount = readCount;
    }

    public void setRecentReadCount(Integer recentReadCount) {
        this.recentReadCount = recentReadCount;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BlogEntityVo)) return false;
        final BlogEntityVo other = (BlogEntityVo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        if (this$title == null ? other$title != null : !this$title.equals(other$title)) return false;
        final Object this$owner = this.getOwner();
        final Object other$owner = other.getOwner();
        if (this$owner == null ? other$owner != null : !this$owner.equals(other$owner)) return false;
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
        final Object this$readCount = this.getReadCount();
        final Object other$readCount = other.getReadCount();
        if (this$readCount == null ? other$readCount != null : !this$readCount.equals(other$readCount)) return false;
        final Object this$recentReadCount = this.getRecentReadCount();
        final Object other$recentReadCount = other.getRecentReadCount();
        if (this$recentReadCount == null ? other$recentReadCount != null : !this$recentReadCount.equals(other$recentReadCount))
            return false;
        final Object this$created = this.getCreated();
        final Object other$created = other.getCreated();
        if (this$created == null ? other$created != null : !this$created.equals(other$created)) return false;
        final Object this$updated = this.getUpdated();
        final Object other$updated = other.getUpdated();
        if (this$updated == null ? other$updated != null : !this$updated.equals(other$updated)) return false;
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BlogEntityVo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        final Object $owner = this.getOwner();
        result = result * PRIME + ($owner == null ? 43 : $owner.hashCode());
        final Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        final Object $content = this.getContent();
        result = result * PRIME + ($content == null ? 43 : $content.hashCode());
        final Object $link = this.getLink();
        result = result * PRIME + ($link == null ? 43 : $link.hashCode());
        final Object $readCount = this.getReadCount();
        result = result * PRIME + ($readCount == null ? 43 : $readCount.hashCode());
        final Object $recentReadCount = this.getRecentReadCount();
        result = result * PRIME + ($recentReadCount == null ? 43 : $recentReadCount.hashCode());
        final Object $created = this.getCreated();
        result = result * PRIME + ($created == null ? 43 : $created.hashCode());
        final Object $updated = this.getUpdated();
        result = result * PRIME + ($updated == null ? 43 : $updated.hashCode());
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        return result;
    }

    public String toString() {
        return "BlogEntityVo(id=" + this.getId() + ", title=" + this.getTitle() + ", owner=" + this.getOwner() + ", description=" + this.getDescription() + ", content=" + this.getContent() + ", link=" + this.getLink() + ", readCount=" + this.getReadCount() + ", recentReadCount=" + this.getRecentReadCount() + ", created=" + this.getCreated() + ", updated=" + this.getUpdated() + ", status=" + this.getStatus() + ")";
    }

    public static class BlogEntityVoBuilder {
        private Long id;
        private String title;
        private Boolean owner;
        private String description;
        private String content;
        private String link;
        private Long readCount;
        private Integer recentReadCount;
        private LocalDateTime created;
        private LocalDateTime updated;
        private Integer status;

        BlogEntityVoBuilder() {
        }

        public BlogEntityVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogEntityVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogEntityVoBuilder owner(Boolean owner) {
            this.owner = owner;
            return this;
        }

        public BlogEntityVoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlogEntityVoBuilder content(String content) {
            this.content = content;
            return this;
        }

        public BlogEntityVoBuilder link(String link) {
            this.link = link;
            return this;
        }

        public BlogEntityVoBuilder readCount(Long readCount) {
            this.readCount = readCount;
            return this;
        }

        public BlogEntityVoBuilder recentReadCount(Integer recentReadCount) {
            this.recentReadCount = recentReadCount;
            return this;
        }

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        public BlogEntityVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        public BlogEntityVoBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public BlogEntityVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BlogEntityVo build() {
            return new BlogEntityVo(this.id, this.title, this.owner, this.description, this.content, this.link, this.readCount, this.recentReadCount, this.created, this.updated, this.status);
        }

        public String toString() {
            return "BlogEntityVo.BlogEntityVoBuilder(id=" + this.id + ", title=" + this.title + ", owner=" + this.owner + ", description=" + this.description + ", content=" + this.content + ", link=" + this.link + ", readCount=" + this.readCount + ", recentReadCount=" + this.recentReadCount + ", created=" + this.created + ", updated=" + this.updated + ", status=" + this.status + ")";
        }
    }
}
