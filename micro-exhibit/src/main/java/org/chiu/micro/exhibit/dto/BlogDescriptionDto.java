package org.chiu.micro.exhibit.dto;


import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author limingjiu
 * @Date 2024/5/10 11:15
 **/
public class BlogDescriptionDto implements Serializable {

    private Long id;

    private String title;

    private String description;

    private Integer status;

    private LocalDateTime created;

    private String link;

    public BlogDescriptionDto(Long id, String title, String description, Integer status, LocalDateTime created, String link) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.created = created;
        this.link = link;
    }

    public BlogDescriptionDto() {
    }

    public static BlogDescriptionDtoBuilder builder() {
        return new BlogDescriptionDtoBuilder();
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

    public Integer getStatus() {
        return this.status;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }

    public String getLink() {
        return this.link;
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

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BlogDescriptionDto)) return false;
        final BlogDescriptionDto other = (BlogDescriptionDto) o;
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
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        final Object this$created = this.getCreated();
        final Object other$created = other.getCreated();
        if (this$created == null ? other$created != null : !this$created.equals(other$created)) return false;
        final Object this$link = this.getLink();
        final Object other$link = other.getLink();
        if (this$link == null ? other$link != null : !this$link.equals(other$link)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BlogDescriptionDto;
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
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        final Object $created = this.getCreated();
        result = result * PRIME + ($created == null ? 43 : $created.hashCode());
        final Object $link = this.getLink();
        result = result * PRIME + ($link == null ? 43 : $link.hashCode());
        return result;
    }

    public String toString() {
        return "BlogDescriptionDto(id=" + this.getId() + ", title=" + this.getTitle() + ", description=" + this.getDescription() + ", status=" + this.getStatus() + ", created=" + this.getCreated() + ", link=" + this.getLink() + ")";
    }

    public static class BlogDescriptionDtoBuilder {
        private Long id;
        private String title;
        private String description;
        private Integer status;
        private LocalDateTime created;
        private String link;

        BlogDescriptionDtoBuilder() {
        }

        public BlogDescriptionDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogDescriptionDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogDescriptionDtoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlogDescriptionDtoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BlogDescriptionDtoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public BlogDescriptionDtoBuilder link(String link) {
            this.link = link;
            return this;
        }

        public BlogDescriptionDto build() {
            return new BlogDescriptionDto(this.id, this.title, this.description, this.status, this.created, this.link);
        }

        public String toString() {
            return "BlogDescriptionDto.BlogDescriptionDtoBuilder(id=" + this.id + ", title=" + this.title + ", description=" + this.description + ", status=" + this.status + ", created=" + this.created + ", link=" + this.link + ")";
        }
    }
}
