package wiki.chiu.micro.exhibit.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;


/**
 * @author mingchiuli
 * @create 2023-03-19 3:27 am
 */
public record BlogExhibitVo(

        String description,

        String nickname,

        String avatar,

        String title,

        String content,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime created,

        Long readCount) {

    public static BlogExhibitVoBuilder builder() {
        return new BlogExhibitVoBuilder(null, null, null, null, null, null, null);
    }

    public record BlogExhibitVoBuilder(
            String description,
            String nickname,
            String avatar,
            String title,
            String content,
            LocalDateTime created,
            Long readCount) {

        public BlogExhibitVoBuilder description(String description) {
            return new BlogExhibitVoBuilder(description, nickname, avatar, title, content, created, readCount);
        }

        public BlogExhibitVoBuilder nickname(String nickname) {
            return new BlogExhibitVoBuilder(description, nickname, avatar, title, content, created, readCount);
        }

        public BlogExhibitVoBuilder avatar(String avatar) {
            return new BlogExhibitVoBuilder(description, nickname, avatar, title, content, created, readCount);
        }

        public BlogExhibitVoBuilder title(String title) {
            return new BlogExhibitVoBuilder(description, nickname, avatar, title, content, created, readCount);
        }

        public BlogExhibitVoBuilder content(String content) {
            return new BlogExhibitVoBuilder(description, nickname, avatar, title, content, created, readCount);
        }

        public BlogExhibitVoBuilder created(LocalDateTime created) {
            return new BlogExhibitVoBuilder(description, nickname, avatar, title, content, created, readCount);
        }

        public BlogExhibitVoBuilder readCount(Long readCount) {
            return new BlogExhibitVoBuilder(description, nickname, avatar, title, content, created, readCount);
        }

        public BlogExhibitVo build() {
            return new BlogExhibitVo(description, nickname, avatar, title, content, created, readCount);
        }

    }
}
