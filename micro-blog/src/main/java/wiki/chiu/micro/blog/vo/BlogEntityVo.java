package wiki.chiu.micro.blog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * @author mingchiuli
 * @create 2022-12-03 11:36 pm
 */
public record BlogEntityVo(
        Long id,

        String title,

        String description,

        String content,

        String link,

        Long readCount,

        Integer recentReadCount,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime created,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updated,

        Integer status) {


    public static BlogEntityVoBuilder builder() {
        return new BlogEntityVoBuilder();
    }


    public static class BlogEntityVoBuilder {
        private Long id;
        private String title;
        private String description;
        private String content;
        private String link;
        private Long readCount;
        private Integer recentReadCount;
        private LocalDateTime created;
        private LocalDateTime updated;
        private Integer status;

        public BlogEntityVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogEntityVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogEntityVoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlogEntityVoBuilder content(String content) {
            this.content = content;
            return this;
        }

        public BlogEntityVoBuilder link(String link) {
            this.link = link;
            return this;
        }

        public BlogEntityVoBuilder readCount(Long readCount) {
            this.readCount = readCount;
            return this;
        }

        public BlogEntityVoBuilder recentReadCount(Integer recentReadCount) {
            this.recentReadCount = recentReadCount;
            return this;
        }

        public BlogEntityVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public BlogEntityVoBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public BlogEntityVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BlogEntityVo build() {
            return new BlogEntityVo(id, title, description, content, link, readCount, recentReadCount, created, updated, status);
        }

    }
}
