package org.chiu.micro.blog.vo;

import java.time.LocalDateTime;

public class BlogEntityRpcVo {

    private Long id;

    private Long userId;

    private String title;

    private String description;

    private Long readCount;

    private String content;

    private String link;

    private LocalDateTime created;

    private LocalDateTime updated;

    private Integer status;

    BlogEntityRpcVo(Long id, Long userId, String title, String description, Long readCount, String content, String link, LocalDateTime created, LocalDateTime updated, Integer status) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.readCount = readCount;
        this.content = content;
        this.link = link;
        this.created = created;
        this.updated = updated;
        this.status = status;
    }

    public static BlogEntityRpcVoBuilder builder() {
        return new BlogEntityRpcVoBuilder();
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

    public Long getReadCount() {
        return this.readCount;
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

    public LocalDateTime getUpdated() {
        return this.updated;
    }

    public Integer getStatus() {
        return this.status;
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

    public void setReadCount(Long readCount) {
        this.readCount = readCount;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setLink(String link) {
        this.link = link;
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

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BlogEntityRpcVo)) return false;
        final BlogEntityRpcVo other = (BlogEntityRpcVo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$userId = this.getUserId();
        final Object other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        if (this$title == null ? other$title != null : !this$title.equals(other$title)) return false;
        final Object this$description = this.getDescription();
        final Object other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description))
            return false;
        final Object this$readCount = this.getReadCount();
        final Object other$readCount = other.getReadCount();
        if (this$readCount == null ? other$readCount != null : !this$readCount.equals(other$readCount)) return false;
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
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BlogEntityRpcVo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $userId = this.getUserId();
        result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        final Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        final Object $readCount = this.getReadCount();
        result = result * PRIME + ($readCount == null ? 43 : $readCount.hashCode());
        final Object $content = this.getContent();
        result = result * PRIME + ($content == null ? 43 : $content.hashCode());
        final Object $link = this.getLink();
        result = result * PRIME + ($link == null ? 43 : $link.hashCode());
        final Object $created = this.getCreated();
        result = result * PRIME + ($created == null ? 43 : $created.hashCode());
        final Object $updated = this.getUpdated();
        result = result * PRIME + ($updated == null ? 43 : $updated.hashCode());
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        return result;
    }

    public String toString() {
        return "BlogEntityRpcVo(id=" + this.getId() + ", userId=" + this.getUserId() + ", title=" + this.getTitle() + ", description=" + this.getDescription() + ", readCount=" + this.getReadCount() + ", content=" + this.getContent() + ", link=" + this.getLink() + ", created=" + this.getCreated() + ", updated=" + this.getUpdated() + ", status=" + this.getStatus() + ")";
    }

    public static class BlogEntityRpcVoBuilder {
        private Long id;
        private Long userId;
        private String title;
        private String description;
        private Long readCount;
        private String content;
        private String link;
        private LocalDateTime created;
        private LocalDateTime updated;
        private Integer status;

        BlogEntityRpcVoBuilder() {
        }

        public BlogEntityRpcVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogEntityRpcVoBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public BlogEntityRpcVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogEntityRpcVoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlogEntityRpcVoBuilder readCount(Long readCount) {
            this.readCount = readCount;
            return this;
        }

        public BlogEntityRpcVoBuilder content(String content) {
            this.content = content;
            return this;
        }

        public BlogEntityRpcVoBuilder link(String link) {
            this.link = link;
            return this;
        }

        public BlogEntityRpcVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public BlogEntityRpcVoBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public BlogEntityRpcVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BlogEntityRpcVo build() {
            return new BlogEntityRpcVo(this.id, this.userId, this.title, this.description, this.readCount, this.content, this.link, this.created, this.updated, this.status);
        }

        public String toString() {
            return "BlogEntityRpcVo.BlogEntityRpcVoBuilder(id=" + this.id + ", userId=" + this.userId + ", title=" + this.title + ", description=" + this.description + ", readCount=" + this.readCount + ", content=" + this.content + ", link=" + this.link + ", created=" + this.created + ", updated=" + this.updated + ", status=" + this.status + ")";
        }
    }
}
