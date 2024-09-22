package org.chiu.micro.exhibit.vo;

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
        return new BlogExhibitVoBuilder();
    }

    public static class BlogExhibitVoBuilder {
        private String description;
        private String nickname;
        private String avatar;
        private String title;
        private String content;
        private LocalDateTime created;
        private Long readCount;

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

        public BlogExhibitVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public BlogExhibitVoBuilder readCount(Long readCount) {
            this.readCount = readCount;
            return this;
        }

        public BlogExhibitVo build() {
            return new BlogExhibitVo(description, nickname, avatar, title, content, created, readCount);
        }

    }
}
