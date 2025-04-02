package wiki.chiu.micro.blog.vo;


import java.util.List;


public record BlogEditVo(

        Long id,

        Long userId,

        String title,

        String description,

        String link,

        String content,

        Integer status,

        List<SensitiveContentVo> sensitiveContentList) {


    public static BlogEditVoBuilder builder() {
        return new BlogEditVoBuilder();
    }

    public static class BlogEditVoBuilder {
        private Long id;
        private Long userId;
        private String title;
        private String description;
        private String link;
        private String content;
        private Integer status;
        private List<SensitiveContentVo> sensitiveContentList;

        public BlogEditVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogEditVoBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public BlogEditVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogEditVoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlogEditVoBuilder link(String link) {
            this.link = link;
            return this;
        }

        public BlogEditVoBuilder content(String content) {
            this.content = content;
            return this;
        }

        public BlogEditVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BlogEditVoBuilder sensitiveContentList(List<SensitiveContentVo> sensitiveContentList) {
            this.sensitiveContentList = sensitiveContentList;
            return this;
        }

        public BlogEditVo build() {
            return new BlogEditVo(id, userId, title, description, link, content, status, sensitiveContentList);
        }

    }


    // 敏感内容DTO
    public record SensitiveContentVo(
            Integer startIndex,
            Integer endIndex,
            Integer type
    ) {
        public static BlogEditVo.SensitiveContentVo.SensitiveContentVoBuilder builder() {
            return new BlogEditVo.SensitiveContentVo.SensitiveContentVoBuilder();
        }

        public record SensitiveContentVoBuilder(
                Integer startIndex,
                Integer endIndex,
                Integer type
        ) {
            public SensitiveContentVoBuilder() {
                this(null, null, null);
            }

            public BlogEditVo.SensitiveContentVo.SensitiveContentVoBuilder startIndex(Integer startIndex) {
                return new BlogEditVo.SensitiveContentVo.SensitiveContentVoBuilder(startIndex, endIndex, type);
            }

            public BlogEditVo.SensitiveContentVo.SensitiveContentVoBuilder endIndex(Integer endIndex) {
                return new BlogEditVo.SensitiveContentVo.SensitiveContentVoBuilder(startIndex, endIndex, type);
            }

            public BlogEditVo.SensitiveContentVo.SensitiveContentVoBuilder type(Integer type) {
                return new BlogEditVo.SensitiveContentVo.SensitiveContentVoBuilder(startIndex, endIndex, type);
            }

            public BlogEditVo.SensitiveContentVo build() {
                return new BlogEditVo.SensitiveContentVo(startIndex, endIndex, type);
            }
        }
    }
}
