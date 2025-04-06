package wiki.chiu.micro.exhibit.dto;


import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author limingjiu
 * @Date 2024/5/10 11:15
 **/
public record BlogDescriptionDto(

        Long id,

        String title,

        String description,

        Integer status,

        LocalDateTime created,

        String link) implements Serializable {

    public static BlogDescriptionDtoBuilder builder() {
        return new BlogDescriptionDtoBuilder(null, null, null, null, null, null);
    }

    public record BlogDescriptionDtoBuilder(
            Long id,
            String title,
            String description,
            Integer status,
            LocalDateTime created,
            String link
    ) {
        public BlogDescriptionDtoBuilder id(Long id) {
            return new BlogDescriptionDtoBuilder(id, title, description, status, created, link);
        }

        public BlogDescriptionDtoBuilder title(String title) {
            return new BlogDescriptionDtoBuilder(id, title, description, status, created, link);
        }

        public BlogDescriptionDtoBuilder description(String description) {
            return new BlogDescriptionDtoBuilder(id, title, description, status, created, link);
        }

        public BlogDescriptionDtoBuilder status(Integer status) {
            return new BlogDescriptionDtoBuilder(id, title, description, status, created, link);
        }

        public BlogDescriptionDtoBuilder created(LocalDateTime created) {
            return new BlogDescriptionDtoBuilder(id, title, description, status, created, link);
        }

        public BlogDescriptionDtoBuilder link(String link) {
            return new BlogDescriptionDtoBuilder(id, title, description, status, created, link);
        }

        public BlogDescriptionDto build() {
            return new BlogDescriptionDto(id, title, description, status, created, link);
        }
    }
}
