package wiki.chiu.micro.blog.dto;


public record BlogEntityDto(

        Long id,

        Long userId,

        String title,

        String description,

        String content,

        Integer status,

        String link) {

    public static BlogEntityDtoBuilder builder() {
        return new BlogEntityDtoBuilder();
    }

    public static class BlogEntityDtoBuilder {
        private Long id;
        private Long userId;
        private String title;
        private String description;
        private String content;
        private Integer status;
        private String link;

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

        public BlogEntityDtoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BlogEntityDtoBuilder link(String link) {
            this.link = link;
            return this;
        }

        public BlogEntityDto build() {
            return new BlogEntityDto(id, userId, title, description, content, status, link);
        }
    }
}
