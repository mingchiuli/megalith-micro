package wiki.chiu.micro.blog.dto;

import java.io.Serializable;
import java.time.LocalDateTime;


public record BlogDeleteDto(

        Long id,

        Long userId,

        String title,

        String description,

        String content,

        LocalDateTime created,

        LocalDateTime updated,

        Integer status,

        String link,

        Long readCount) implements Serializable {

    public static BlogEntityDtoBuilder builder() {
        return new BlogEntityDtoBuilder(null, null, null, null, null, null, null, null, null, null);
    }

    public record BlogEntityDtoBuilder(
            Long id,
            Long userId,
            String title,
            String description,
            String content,
            LocalDateTime created,
            LocalDateTime updated,
            Integer status,
            String link,
            Long readCount
    ) {


        public BlogEntityDtoBuilder id(Long id) {
            return new BlogEntityDtoBuilder(id, userId, title, description, content, created, updated, status, link, readCount);
        }

        public BlogEntityDtoBuilder userId(Long userId) {
            return new BlogEntityDtoBuilder(id, userId, title, description, content, created, updated, status, link, readCount);
        }

        public BlogEntityDtoBuilder title(String title) {
            return new BlogEntityDtoBuilder(id, userId, title, description, content, created, updated, status, link, readCount);
        }

        public BlogEntityDtoBuilder description(String description) {
            return new BlogEntityDtoBuilder(id, userId, title, description, content, created, updated, status, link, readCount);
        }

        public BlogEntityDtoBuilder content(String content) {
            return new BlogEntityDtoBuilder(id, userId, title, description, content, created, updated, status, link, readCount);
        }

        public BlogEntityDtoBuilder created(LocalDateTime created) {
            return new BlogEntityDtoBuilder(id, userId, title, description, content, created, updated, status, link, readCount);
        }

        public BlogEntityDtoBuilder updated(LocalDateTime updated) {
            return new BlogEntityDtoBuilder(id, userId, title, description, content, created, updated, status, link, readCount);
        }

        public BlogEntityDtoBuilder status(Integer status) {
            return new BlogEntityDtoBuilder(id, userId, title, description, content, created, updated, status, link, readCount);
        }

        public BlogEntityDtoBuilder link(String link) {
            return new BlogEntityDtoBuilder(id, userId, title, description, content, created, updated, status, link, readCount);
        }

        public BlogEntityDtoBuilder readCount(Long readCount) {
            return new BlogEntityDtoBuilder(id, userId, title, description, content, created, updated, status, link, readCount);
        }

        public BlogDeleteDto build() {
            return new BlogDeleteDto(id, userId, title, description, content, created, updated, status, link, readCount);
        }
    }
}
