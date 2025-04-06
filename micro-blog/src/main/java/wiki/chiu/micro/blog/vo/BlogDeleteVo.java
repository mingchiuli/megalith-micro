package wiki.chiu.micro.blog.vo;


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
        return new BlogDeleteVoBuilder(null, null, null, null, null, null, null, null, null, null, null);
    }

    public record BlogDeleteVoBuilder(
            Long id,
            Long userId,
            String title,
            String description,
            String content,
            LocalDateTime created,
            LocalDateTime updated,
            Integer status,
            Integer idx,
            String link,
            Long readCount
    ) {

        public BlogDeleteVoBuilder id(Long id) {
            return new BlogDeleteVoBuilder(id, userId, title, description, content, created, updated, status, idx, link, readCount);
        }

        public BlogDeleteVoBuilder userId(Long userId) {
            return new BlogDeleteVoBuilder(id, userId, title, description, content, created, updated, status, idx, link, readCount);
        }

        public BlogDeleteVoBuilder title(String title) {
            return new BlogDeleteVoBuilder(id, userId, title, description, content, created, updated, status, idx, link, readCount);
        }

        public BlogDeleteVoBuilder description(String description) {
            return new BlogDeleteVoBuilder(id, userId, title, description, content, created, updated, status, idx, link, readCount);
        }

        public BlogDeleteVoBuilder content(String content) {
            return new BlogDeleteVoBuilder(id, userId, title, description, content, created, updated, status, idx, link, readCount);
        }

        public BlogDeleteVoBuilder created(LocalDateTime created) {
            return new BlogDeleteVoBuilder(id, userId, title, description, content, created, updated, status, idx, link, readCount);
        }

        public BlogDeleteVoBuilder updated(LocalDateTime updated) {
            return new BlogDeleteVoBuilder(id, userId, title, description, content, created, updated, status, idx, link, readCount);
        }

        public BlogDeleteVoBuilder status(Integer status) {
            return new BlogDeleteVoBuilder(id, userId, title, description, content, created, updated, status, idx, link, readCount);
        }

        public BlogDeleteVoBuilder idx(Integer idx) {
            return new BlogDeleteVoBuilder(id, userId, title, description, content, created, updated, status, idx, link, readCount);
        }

        public BlogDeleteVoBuilder link(String link) {
            return new BlogDeleteVoBuilder(id, userId, title, description, content, created, updated, status, idx, link, readCount);
        }

        public BlogDeleteVoBuilder readCount(Long readCount) {
            return new BlogDeleteVoBuilder(id, userId, title, description, content, created, updated, status, idx, link, readCount);
        }

        public BlogDeleteVo build() {
            return new BlogDeleteVo(id, userId, title, description, content, created, updated, status, idx, link, readCount);
        }
    }
}
