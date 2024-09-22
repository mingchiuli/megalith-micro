package org.chiu.micro.blog.vo;

import java.util.List;


public class BlogEditVo {

    private Long id;

    private Long userId;

    private String title;

    private String description;

    private String link;

    private String content;

    private Integer status;

    private Integer version;

    private List<SensitiveContentVo> sensitiveContentList;

    BlogEditVo(Long id, Long userId, String title, String description, String link, String content, Integer status, Integer version, List<SensitiveContentVo> sensitiveContentList) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.link = link;
        this.content = content;
        this.status = status;
        this.version = version;
        this.sensitiveContentList = sensitiveContentList;
    }

    public static BlogEditVoBuilder builder() {
        return new BlogEditVoBuilder();
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

    public String getLink() {
        return this.link;
    }

    public String getContent() {
        return this.content;
    }

    public Integer getStatus() {
        return this.status;
    }

    public Integer getVersion() {
        return this.version;
    }

    public List<SensitiveContentVo> getSensitiveContentList() {
        return this.sensitiveContentList;
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

    public void setLink(String link) {
        this.link = link;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setSensitiveContentList(List<SensitiveContentVo> sensitiveContentList) {
        this.sensitiveContentList = sensitiveContentList;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BlogEditVo)) return false;
        final BlogEditVo other = (BlogEditVo) o;
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
        final Object this$link = this.getLink();
        final Object other$link = other.getLink();
        if (this$link == null ? other$link != null : !this$link.equals(other$link)) return false;
        final Object this$content = this.getContent();
        final Object other$content = other.getContent();
        if (this$content == null ? other$content != null : !this$content.equals(other$content)) return false;
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        final Object this$version = this.getVersion();
        final Object other$version = other.getVersion();
        if (this$version == null ? other$version != null : !this$version.equals(other$version)) return false;
        final Object this$sensitiveContentList = this.getSensitiveContentList();
        final Object other$sensitiveContentList = other.getSensitiveContentList();
        if (this$sensitiveContentList == null ? other$sensitiveContentList != null : !this$sensitiveContentList.equals(other$sensitiveContentList))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BlogEditVo;
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
        final Object $link = this.getLink();
        result = result * PRIME + ($link == null ? 43 : $link.hashCode());
        final Object $content = this.getContent();
        result = result * PRIME + ($content == null ? 43 : $content.hashCode());
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        final Object $version = this.getVersion();
        result = result * PRIME + ($version == null ? 43 : $version.hashCode());
        final Object $sensitiveContentList = this.getSensitiveContentList();
        result = result * PRIME + ($sensitiveContentList == null ? 43 : $sensitiveContentList.hashCode());
        return result;
    }

    public String toString() {
        return "BlogEditVo(id=" + this.getId() + ", userId=" + this.getUserId() + ", title=" + this.getTitle() + ", description=" + this.getDescription() + ", link=" + this.getLink() + ", content=" + this.getContent() + ", status=" + this.getStatus() + ", version=" + this.getVersion() + ", sensitiveContentList=" + this.getSensitiveContentList() + ")";
    }

    public static class BlogEditVoBuilder {
        private Long id;
        private Long userId;
        private String title;
        private String description;
        private String link;
        private String content;
        private Integer status;
        private Integer version;
        private List<SensitiveContentVo> sensitiveContentList;

        BlogEditVoBuilder() {
        }

        public BlogEditVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogEditVoBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public BlogEditVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogEditVoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlogEditVoBuilder link(String link) {
            this.link = link;
            return this;
        }

        public BlogEditVoBuilder content(String content) {
            this.content = content;
            return this;
        }

        public BlogEditVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BlogEditVoBuilder version(Integer version) {
            this.version = version;
            return this;
        }

        public BlogEditVoBuilder sensitiveContentList(List<SensitiveContentVo> sensitiveContentList) {
            this.sensitiveContentList = sensitiveContentList;
            return this;
        }

        public BlogEditVo build() {
            return new BlogEditVo(this.id, this.userId, this.title, this.description, this.link, this.content, this.status, this.version, this.sensitiveContentList);
        }

        public String toString() {
            return "BlogEditVo.BlogEditVoBuilder(id=" + this.id + ", userId=" + this.userId + ", title=" + this.title + ", description=" + this.description + ", link=" + this.link + ", content=" + this.content + ", status=" + this.status + ", version=" + this.version + ", sensitiveContentList=" + this.sensitiveContentList + ")";
        }
    }
}
