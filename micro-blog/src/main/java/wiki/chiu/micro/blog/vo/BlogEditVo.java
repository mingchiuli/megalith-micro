package wiki.chiu.micro.blog.vo;


import java.util.Collections;
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
        return new BlogEditVoBuilder(null, null, null, null, null, null, null, Collections.emptyList());
    }

    public record BlogEditVoBuilder(

        Long id,

        Long userId,

        String title,

        String description,

        String link,

        String content,

        Integer status,

        List<SensitiveContentVo> sensitiveContentList) {

        public BlogEditVoBuilder id(Long id) {
            return new BlogEditVoBuilder(id, userId, title, description, link, content, status, sensitiveContentList);
        }

        public BlogEditVoBuilder userId(Long userId) {
            return new BlogEditVoBuilder(id, userId, title, description, link, content, status, sensitiveContentList);
        }

        public BlogEditVoBuilder title(String title) {
            return new BlogEditVoBuilder(id, userId, title, description, link, content, status, sensitiveContentList);
        }

        public BlogEditVoBuilder description(String description) {
            return new BlogEditVoBuilder(id, userId, title, description, link, content, status, sensitiveContentList);
        }

        public BlogEditVoBuilder link(String link) {
            return new BlogEditVoBuilder(id, userId, title, description, link, content, status, sensitiveContentList);
        }

        public BlogEditVoBuilder content(String content) {
            return new BlogEditVoBuilder(id, userId, title, description, link, content, status, sensitiveContentList);
        }

        public BlogEditVoBuilder status(Integer status) {
            return new BlogEditVoBuilder(id, userId, title, description, link, content, status, sensitiveContentList);
        }

        public BlogEditVoBuilder sensitiveContentList(List<SensitiveContentVo> sensitiveContentList) {
            return new BlogEditVoBuilder(id, userId, title, description, link, content, status, sensitiveContentList);
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
