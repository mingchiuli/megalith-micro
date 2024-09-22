package org.chiu.micro.exhibit.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * @author mingchiuli
 * @create 2023-03-19 3:27 am
 */
public class BlogExhibitVo {

    private String description;

    private String nickname;

    private String avatar;

    private String title;

    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    private Long readCount;

    public BlogExhibitVo(String description, String nickname, String avatar, String title, String content, LocalDateTime created, Long readCount) {
        this.description = description;
        this.nickname = nickname;
        this.avatar = avatar;
        this.title = title;
        this.content = content;
        this.created = created;
        this.readCount = readCount;
    }

    public BlogExhibitVo() {
    }

    public static BlogExhibitVoBuilder builder() {
        return new BlogExhibitVoBuilder();
    }

    public String getDescription() {
        return this.description;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }

    public Long getReadCount() {
        return this.readCount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setReadCount(Long readCount) {
        this.readCount = readCount;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BlogExhibitVo)) return false;
        final BlogExhibitVo other = (BlogExhibitVo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$description = this.getDescription();
        final Object other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description))
            return false;
        final Object this$nickname = this.getNickname();
        final Object other$nickname = other.getNickname();
        if (this$nickname == null ? other$nickname != null : !this$nickname.equals(other$nickname)) return false;
        final Object this$avatar = this.getAvatar();
        final Object other$avatar = other.getAvatar();
        if (this$avatar == null ? other$avatar != null : !this$avatar.equals(other$avatar)) return false;
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        if (this$title == null ? other$title != null : !this$title.equals(other$title)) return false;
        final Object this$content = this.getContent();
        final Object other$content = other.getContent();
        if (this$content == null ? other$content != null : !this$content.equals(other$content)) return false;
        final Object this$created = this.getCreated();
        final Object other$created = other.getCreated();
        if (this$created == null ? other$created != null : !this$created.equals(other$created)) return false;
        final Object this$readCount = this.getReadCount();
        final Object other$readCount = other.getReadCount();
        if (this$readCount == null ? other$readCount != null : !this$readCount.equals(other$readCount)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BlogExhibitVo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        final Object $nickname = this.getNickname();
        result = result * PRIME + ($nickname == null ? 43 : $nickname.hashCode());
        final Object $avatar = this.getAvatar();
        result = result * PRIME + ($avatar == null ? 43 : $avatar.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        final Object $content = this.getContent();
        result = result * PRIME + ($content == null ? 43 : $content.hashCode());
        final Object $created = this.getCreated();
        result = result * PRIME + ($created == null ? 43 : $created.hashCode());
        final Object $readCount = this.getReadCount();
        result = result * PRIME + ($readCount == null ? 43 : $readCount.hashCode());
        return result;
    }

    public String toString() {
        return "BlogExhibitVo(description=" + this.getDescription() + ", nickname=" + this.getNickname() + ", avatar=" + this.getAvatar() + ", title=" + this.getTitle() + ", content=" + this.getContent() + ", created=" + this.getCreated() + ", readCount=" + this.getReadCount() + ")";
    }

    public static class BlogExhibitVoBuilder {
        private String description;
        private String nickname;
        private String avatar;
        private String title;
        private String content;
        private LocalDateTime created;
        private Long readCount;

        BlogExhibitVoBuilder() {
        }

        public BlogExhibitVoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlogExhibitVoBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public BlogExhibitVoBuilder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public BlogExhibitVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogExhibitVoBuilder content(String content) {
            this.content = content;
            return this;
        }

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        public BlogExhibitVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public BlogExhibitVoBuilder readCount(Long readCount) {
            this.readCount = readCount;
            return this;
        }

        public BlogExhibitVo build() {
            return new BlogExhibitVo(this.description, this.nickname, this.avatar, this.title, this.content, this.created, this.readCount);
        }

        public String toString() {
            return "BlogExhibitVo.BlogExhibitVoBuilder(description=" + this.description + ", nickname=" + this.nickname + ", avatar=" + this.avatar + ", title=" + this.title + ", content=" + this.content + ", created=" + this.created + ", readCount=" + this.readCount + ")";
        }
    }
}
