package wiki.chiu.micro.blog.dto;

public record BlogEntityDto(
    Long id,

    Long userId,

    String title,

    String description,

    String content,

    Integer status,

    String link
) {
    public static BlogEntityDtoBuilder builder() {
        return new BlogEntityDtoBuilder(null, null, null, null, null, null, null);
    }

    public record BlogEntityDtoBuilder(
        Long id,
        Long userId,
        String title,
        String description,
        String content,
        Integer status,
        String link
    ) {
        public BlogEntityDtoBuilder id(Long id) {
            return new BlogEntityDtoBuilder(
                id,
                userId,
                title,
                description,
                content,
                status,
                link
            );
        }

        public BlogEntityDtoBuilder userId(Long userId) {
            return new BlogEntityDtoBuilder(
                id,
                userId,
                title,
                description,
                content,
                status,
                link
            );
        }

        public BlogEntityDtoBuilder title(String title) {
            return new BlogEntityDtoBuilder(
                id,
                userId,
                title,
                description,
                content,
                status,
                link
            );
        }

        public BlogEntityDtoBuilder description(String description) {
            return new BlogEntityDtoBuilder(
                id,
                userId,
                title,
                description,
                content,
                status,
                link
            );
        }

        public BlogEntityDtoBuilder content(String content) {
            return new BlogEntityDtoBuilder(
                id,
                userId,
                title,
                description,
                content,
                status,
                link
            );
        }

        public BlogEntityDtoBuilder status(Integer status) {
            return new BlogEntityDtoBuilder(
                id,
                userId,
                title,
                description,
                content,
                status,
                link
            );
        }

        public BlogEntityDtoBuilder link(String link) {
            return new BlogEntityDtoBuilder(
                id,
                userId,
                title,
                description,
                content,
                status,
                link
            );
        }

        public BlogEntityDto build() {
            return new BlogEntityDto(
                id,
                userId,
                title,
                description,
                content,
                status,
                link
            );
        }
    }
}
