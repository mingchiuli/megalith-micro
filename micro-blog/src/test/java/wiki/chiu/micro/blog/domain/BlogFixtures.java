package wiki.chiu.micro.blog.domain;

import java.time.LocalDateTime;

/**
 * Builds domain blogs for tests, mirroring the field set the services pass around.
 */
public final class BlogFixtures {

    private BlogFixtures() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

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
        private Long eventRevision;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public Builder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder link(String link) {
            this.link = link;
            return this;
        }

        public Builder readCount(Long readCount) {
            this.readCount = readCount;
            return this;
        }

        public Builder eventRevision(Long eventRevision) {
            this.eventRevision = eventRevision;
            return this;
        }

        public Blog build() {
            return new Blog(
                id, userId, title, description, content, created, updated, status, link, readCount,
                eventRevision);
        }
    }
}
