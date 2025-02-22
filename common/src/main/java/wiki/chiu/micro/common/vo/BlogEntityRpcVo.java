package wiki.chiu.micro.common.vo;

import java.time.LocalDateTime;


public record BlogEntityRpcVo(

        Long id,

        Long userId,

        String title,

        String description,

        String content,

        LocalDateTime created,

        LocalDateTime updated,

        Integer status,

        String link,

        Long readCount) {

    public BlogEntityRpcVo(Long blogId) {
        this(blogId, null, null, null, null, null, null, null, null, null);
    }

    public static BlogEntityDtoBuilder builder() {
        return new BlogEntityDtoBuilder();
    }

    public static class BlogEntityDtoBuilder {
        private Long id;
        private Long userId;
        private String title;
        private String description;
        private String content;
        private LocalDateTime created;
        private LocalDateTime updated;
        private Integer status;
        private String link;
        private Long readCount;

        public BlogEntityDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogEntityDtoBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public BlogEntityDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogEntityDtoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlogEntityDtoBuilder content(String content) {
            this.content = content;
            return this;
        }

        public BlogEntityDtoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public BlogEntityDtoBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public BlogEntityDtoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BlogEntityDtoBuilder link(String link) {
            this.link = link;
            return this;
        }

        public BlogEntityDtoBuilder readCount(Long readCount) {
            this.readCount = readCount;
            return this;
        }

        public BlogEntityRpcVo build() {
            return new BlogEntityRpcVo(id, userId, title, description, content, created, updated, status, link, readCount);
        }
    }
}
