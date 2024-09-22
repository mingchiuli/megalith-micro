package org.chiu.micro.exhibit.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * @author mingchiuli
 * @create 2023-04-12 1:05 pm
 */
public class BlogDescriptionVo {

    private Long id;

    private String title;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    private String link;

    public BlogDescriptionVo(Long id, String title, String description, LocalDateTime created, String link) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.created = created;
        this.link = link;
    }

    public BlogDescriptionVo() {
    }

    public static BlogDescriptionVoBuilder builder() {
        return new BlogDescriptionVoBuilder();
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BlogDescriptionVo)) return false;
        final BlogDescriptionVo other = (BlogDescriptionVo) o;
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
        final Object this$created = this.getCreated();
        final Object other$created = other.getCreated();
        if (this$created == null ? other$created != null : !this$created.equals(other$created)) return false;
        final Object this$link = this.getLink();
        final Object other$link = other.getLink();
        if (this$link == null ? other$link != null : !this$link.equals(other$link)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BlogDescriptionVo;
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
        final Object $created = this.getCreated();
        result = result * PRIME + ($created == null ? 43 : $created.hashCode());
        final Object $link = this.getLink();
        result = result * PRIME + ($link == null ? 43 : $link.hashCode());
        return result;
    }

    public String toString() {
        return "BlogDescriptionVo(id=" + this.getId() + ", title=" + this.getTitle() + ", description=" + this.getDescription() + ", created=" + this.getCreated() + ", link=" + this.getLink() + ")";
    }

    public static class BlogDescriptionVoBuilder {
        private Long id;
        private String title;
        private String description;
        private LocalDateTime created;
        private String link;

        BlogDescriptionVoBuilder() {
        }

        public BlogDescriptionVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogDescriptionVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogDescriptionVoBuilder description(String description) {
            this.description = description;
            return this;
        }

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        public BlogDescriptionVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public BlogDescriptionVoBuilder link(String link) {
            this.link = link;
            return this;
        }

        public BlogDescriptionVo build() {
            return new BlogDescriptionVo(this.id, this.title, this.description, this.created, this.link);
        }

        public String toString() {
            return "BlogDescriptionVo.BlogDescriptionVoBuilder(id=" + this.id + ", title=" + this.title + ", description=" + this.description + ", created=" + this.created + ", link=" + this.link + ")";
        }
    }
}
