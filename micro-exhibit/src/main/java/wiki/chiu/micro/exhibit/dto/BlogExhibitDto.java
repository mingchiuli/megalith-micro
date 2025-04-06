package wiki.chiu.micro.exhibit.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record BlogExhibitDto(

        Long userId,

        String description,

        String nickname,

        String avatar,

        String title,

        String content,

        LocalDateTime created,

        Long readCount) implements Serializable {

    public static BlogExhibitDtoBuilder builder() {
        return new BlogExhibitDtoBuilder();
    }

    public static class BlogExhibitDtoBuilder {
        private Long userId;
        private String description;
        private String nickname;
        private String avatar;
        private String title;
        private String content;
        private LocalDateTime created;
        private Long readCount;

        public BlogExhibitDtoBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public BlogExhibitDtoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlogExhibitDtoBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public BlogExhibitDtoBuilder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public BlogExhibitDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogExhibitDtoBuilder content(String content) {
            this.content = content;
            return this;
        }

        public BlogExhibitDtoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public BlogExhibitDtoBuilder readCount(Long readCount) {
            this.readCount = readCount;
            return this;
        }

        public BlogExhibitDto build() {
            return new BlogExhibitDto(userId, description, nickname, avatar, title, content, created, readCount);
        }
    }
}
