package org.chiu.micro.blog.dto;

import java.time.LocalDateTime;


public class BlogEntityDto {

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

    BlogEntityDto(Long id, Long userId, String title, String description, String content, LocalDateTime created, LocalDateTime updated, Integer status, String link, Long readCount) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.content = content;
        this.created = created;
        this.updated = updated;
        this.status = status;
        this.link = link;
        this.readCount = readCount;
    }

    public static BlogEntityDtoBuilder builder() {
        return new BlogEntityDtoBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getContent() {
        return this.content;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }

    public LocalDateTime getUpdated() {
        return this.updated;
    }

    public Integer getStatus() {
        return this.status;
    }

    public String getLink() {
        return this.link;
    }

    public Long getReadCount() {
        return this.readCount;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setReadCount(Long readCount) {
        this.readCount = readCount;
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

        public BlogEntityDto build() {
            return new BlogEntityDto(id, userId, title, description, content, created, updated, status, link, readCount);
        }
    }
}
