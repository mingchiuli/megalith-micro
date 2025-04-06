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

    public static BlogEntityRpcVoBuilder builder() {
        return new BlogEntityRpcVoBuilder(null, null, null, null, null, null, null, null, null, null);
    }

    public record BlogEntityRpcVoBuilder(
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

        public BlogEntityRpcVoBuilder id(Long id) {
            return new BlogEntityRpcVoBuilder(id, this.userId, this.title, this.description, this.content, this.created, this.updated, this.status, this.link, this.readCount);
        }

        public BlogEntityRpcVoBuilder userId(Long userId) {
            return new BlogEntityRpcVoBuilder(this.id, userId, this.title, this.description, this.content, this.created, this.updated, this.status, this.link, this.readCount);
        }

        public BlogEntityRpcVoBuilder title(String title) {
            return new BlogEntityRpcVoBuilder(this.id, this.userId, title, this.description, this.content, this.created, this.updated, this.status, this.link, this.readCount);
        }

        public BlogEntityRpcVoBuilder description(String description) {
            return new BlogEntityRpcVoBuilder(this.id, this.userId, this.title, description, this.content, this.created, this.updated, this.status, this.link, this.readCount);
        }

        public BlogEntityRpcVoBuilder content(String content) {
            return new BlogEntityRpcVoBuilder(this.id, this.userId, this.title, this.description, content, this.created, this.updated, this.status, this.link, this.readCount);
        }

        public BlogEntityRpcVoBuilder created(LocalDateTime created) {
            return new BlogEntityRpcVoBuilder(this.id, this.userId, this.title, this.description, this.content, created, this.updated, this.status, this.link, this.readCount);
        }

        public BlogEntityRpcVoBuilder updated(LocalDateTime updated) {
            return new BlogEntityRpcVoBuilder(this.id, this.userId, this.title, this.description, this.content, this.created, updated, this.status, this.link, this.readCount);
        }

        public BlogEntityRpcVoBuilder status(Integer status) {
            return new BlogEntityRpcVoBuilder(this.id, this.userId, this.title, this.description, this.content, this.created, this.updated, status, this.link, this.readCount);
        }

        public BlogEntityRpcVoBuilder link(String link) {
            return new BlogEntityRpcVoBuilder(this.id, this.userId, this.title, this.description, this.content, this.created, this.updated, this.status, link, this.readCount);
        }

        public BlogEntityRpcVoBuilder readCount(Long readCount) {
            return new BlogEntityRpcVoBuilder(this.id, this.userId, this.title, this.description, this.content, this.created, this.updated, this.status, this.link, readCount);
        }

        public BlogEntityRpcVo build() {
            return new BlogEntityRpcVo(id, userId, title, description, content, created, updated, status, link, readCount);
        }
    }
}