package org.chiu.micro.blog.vo;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record BlogDeleteVo(

        Long id,

        Long userId,

        String title,

        String description,

        String content,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime created,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updated,

        Integer status,

        Integer idx,

        String link,

        Long readCount) {

    public static BlogDeleteVoBuilder builder() {
        return new BlogDeleteVoBuilder();
    }

    public static class BlogDeleteVoBuilder {
        private Long id;
        private Long userId;
        private String title;
        private String description;
        private String content;
        private LocalDateTime created;
        private LocalDateTime updated;
        private Integer status;
        private Integer idx;
        private String link;
        private Long readCount;

        public BlogDeleteVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogDeleteVoBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public BlogDeleteVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogDeleteVoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlogDeleteVoBuilder content(String content) {
            this.content = content;
            return this;
        }

        public BlogDeleteVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public BlogDeleteVoBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public BlogDeleteVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BlogDeleteVoBuilder idx(Integer idx) {
            this.idx = idx;
            return this;
        }

        public BlogDeleteVoBuilder link(String link) {
            this.link = link;
            return this;
        }

        public BlogDeleteVoBuilder readCount(Long readCount) {
            this.readCount = readCount;
            return this;
        }

        public BlogDeleteVo build() {
            return new BlogDeleteVo(id, userId, title, description, content, created, updated, status, idx, link, readCount);
        }
    }
}
