package wiki.chiu.micro.exhibit.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * @author mingchiuli
 * @create 2023-04-12 1:05 pm
 */
public record BlogDescriptionVo(

        Long id,

        String title,

        String description,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime created,

        String link) {

    public static BlogDescriptionVoBuilder builder() {
        return new BlogDescriptionVoBuilder();
    }

    public static class BlogDescriptionVoBuilder {
        private Long id;
        private String title;
        private String description;
        private LocalDateTime created;
        private String link;

        public BlogDescriptionVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogDescriptionVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogDescriptionVoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlogDescriptionVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public BlogDescriptionVoBuilder link(String link) {
            this.link = link;
            return this;
        }

        public BlogDescriptionVo build() {
            return new BlogDescriptionVo(id, title, description, created, link);
        }
    }
}
