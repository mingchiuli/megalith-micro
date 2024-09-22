package org.chiu.micro.blog.req;

import java.util.List;

public class BlogEditPushAllReq {

    private Long id;

    private String title;

    private String description;

    private String content;

    private Integer status;

    private String link;

    private Integer version;

    private List<SensitiveContentReq> sensitiveContentList;

    public BlogEditPushAllReq() {
    }

    public Long getId() {
        return this.id;
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

    public Integer getStatus() {
        return this.status;
    }

    public String getLink() {
        return this.link;
    }

    public Integer getVersion() {
        return this.version;
    }

    public List<SensitiveContentReq> getSensitiveContentList() {
        return this.sensitiveContentList;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setSensitiveContentList(List<SensitiveContentReq> sensitiveContentList) {
        this.sensitiveContentList = sensitiveContentList;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BlogEditPushAllReq)) return false;
        final BlogEditPushAllReq other = (BlogEditPushAllReq) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
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
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        final Object this$link = this.getLink();
        final Object other$link = other.getLink();
        if (this$link == null ? other$link != null : !this$link.equals(other$link)) return false;
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
        return other instanceof BlogEditPushAllReq;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        final Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        final Object $content = this.getContent();
        result = result * PRIME + ($content == null ? 43 : $content.hashCode());
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        final Object $link = this.getLink();
        result = result * PRIME + ($link == null ? 43 : $link.hashCode());
        final Object $version = this.getVersion();
        result = result * PRIME + ($version == null ? 43 : $version.hashCode());
        final Object $sensitiveContentList = this.getSensitiveContentList();
        result = result * PRIME + ($sensitiveContentList == null ? 43 : $sensitiveContentList.hashCode());
        return result;
    }

    public String toString() {
        return "BlogEditPushAllReq(id=" + this.getId() + ", title=" + this.getTitle() + ", description=" + this.getDescription() + ", content=" + this.getContent() + ", status=" + this.getStatus() + ", link=" + this.getLink() + ", version=" + this.getVersion() + ", sensitiveContentList=" + this.getSensitiveContentList() + ")";
    }
}
