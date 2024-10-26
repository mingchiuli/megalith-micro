package wiki.chiu.micro.blog.vo;

import java.time.LocalDateTime;

public record BlogEntityRpcVo(

        Long id,

        Long userId,

        String title,

        String description,

        Long readCount,

        String content,

        String link,

        LocalDateTime created,

        LocalDateTime updated,

        Integer status) {

    public static BlogEntityRpcVoBuilder builder() {
        return new BlogEntityRpcVoBuilder();
    }


    public static class BlogEntityRpcVoBuilder {
        private Long id;
        private Long userId;
        private String title;
        private String description;
        private Long readCount;
        private String content;
        private String link;
        private LocalDateTime created;
        private LocalDateTime updated;
        private Integer status;

        public BlogEntityRpcVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogEntityRpcVoBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public BlogEntityRpcVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogEntityRpcVoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlogEntityRpcVoBuilder readCount(Long readCount) {
            this.readCount = readCount;
            return this;
        }

        public BlogEntityRpcVoBuilder content(String content) {
            this.content = content;
            return this;
        }

        public BlogEntityRpcVoBuilder link(String link) {
            this.link = link;
            return this;
        }

        public BlogEntityRpcVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public BlogEntityRpcVoBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public BlogEntityRpcVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BlogEntityRpcVo build() {
            return new BlogEntityRpcVo(id, userId, title, description, readCount, content, link, created, updated, status);
        }
    }
}
