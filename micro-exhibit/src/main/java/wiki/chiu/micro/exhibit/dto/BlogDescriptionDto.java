package wiki.chiu.micro.exhibit.dto;


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

        String link) {

    public static BlogDescriptionDtoBuilder builder() {
        return new BlogDescriptionDtoBuilder();
    }

    public static class BlogDescriptionDtoBuilder {
        private Long id;
        private String title;
        private String description;
        private Integer status;
        private LocalDateTime created;
        private String link;

        public BlogDescriptionDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogDescriptionDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogDescriptionDtoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlogDescriptionDtoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BlogDescriptionDtoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public BlogDescriptionDtoBuilder link(String link) {
            this.link = link;
            return this;
        }

        public BlogDescriptionDto build() {
            return new BlogDescriptionDto(id, title, description, status, created, link);
        }
    }
}
