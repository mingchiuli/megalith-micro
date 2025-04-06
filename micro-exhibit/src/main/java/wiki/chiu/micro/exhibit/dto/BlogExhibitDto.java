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
        return new BlogExhibitDtoBuilder(null, null, null, null, null, null, null, null);
    }

    public record BlogExhibitDtoBuilder(
            Long userId,
            String description,
            String nickname,
            String avatar,
            String title,
            String content,
            LocalDateTime created,
            Long readCount
    ) {
        public BlogExhibitDtoBuilder userId(Long userId) {
            return new BlogExhibitDtoBuilder(userId, description, nickname, avatar, title, content, created, readCount);
        }

        public BlogExhibitDtoBuilder description(String description) {
            return new BlogExhibitDtoBuilder(userId, description, nickname, avatar, title, content, created, readCount);
        }

        public BlogExhibitDtoBuilder nickname(String nickname) {
            return new BlogExhibitDtoBuilder(userId, description, nickname, avatar, title, content, created, readCount);
        }

        public BlogExhibitDtoBuilder avatar(String avatar) {
            return new BlogExhibitDtoBuilder(userId, description, nickname, avatar, title, content, created, readCount);
        }

        public BlogExhibitDtoBuilder title(String title) {
            return new BlogExhibitDtoBuilder(userId, description, nickname, avatar, title, content, created, readCount);
        }

        public BlogExhibitDtoBuilder content(String content) {
            return new BlogExhibitDtoBuilder(userId, description, nickname, avatar, title, content, created, readCount);
        }

        public BlogExhibitDtoBuilder created(LocalDateTime created) {
            return new BlogExhibitDtoBuilder(userId, description, nickname, avatar, title, content, created, readCount);
        }

        public BlogExhibitDtoBuilder readCount(Long readCount) {
            return new BlogExhibitDtoBuilder(userId, description, nickname, avatar, title, content, created, readCount);
        }

        public BlogExhibitDto build() {
            return new BlogExhibitDto(userId, description, nickname, avatar, title, content, created, readCount);
        }
    }
}
