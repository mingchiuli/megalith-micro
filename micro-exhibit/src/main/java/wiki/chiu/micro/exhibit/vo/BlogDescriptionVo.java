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
        return new BlogDescriptionVoBuilder(null, null, null, null, null);
    }

    public record BlogDescriptionVoBuilder(
            Long id,
            String title,
            String description,
            LocalDateTime created,
            String link
    ) {
        public BlogDescriptionVoBuilder id(Long id) {
            return new BlogDescriptionVoBuilder(id, title, description, created, link);
        }

        public BlogDescriptionVoBuilder title(String title) {
            return new BlogDescriptionVoBuilder(id, title, description, created, link);
        }

        public BlogDescriptionVoBuilder description(String description) {
            return new BlogDescriptionVoBuilder(id, title, description, created, link);
        }

        public BlogDescriptionVoBuilder created(LocalDateTime created) {
            return new BlogDescriptionVoBuilder(id, title, description, created, link);
        }

        public BlogDescriptionVoBuilder link(String link) {
            return new BlogDescriptionVoBuilder(id, title, description, created, link);
        }

        public BlogDescriptionVo build() {
            return new BlogDescriptionVo(id, title, description, created, link);
        }
    }
}
