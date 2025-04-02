package wiki.chiu.micro.blog.dto;


import java.io.Serializable;
import java.util.List;

public record BlogPushAllDto(
        Long id,
        String title,
        String description,
        String content,
        Integer status,
        String link,
        List<SensitiveContentDto> sensitiveContentList
) implements Serializable {

    // Builder方法
    public static BlogPushAllDtoBuilder builder() {
        return new BlogPushAllDtoBuilder();
    }

    // Builder记录类
    public record BlogPushAllDtoBuilder(
            Long id,
            String title,
            String description,
            String content,
            Integer status,
            String link,
            List<SensitiveContentDto> sensitiveContentList
    ) {

        public BlogPushAllDtoBuilder() {
            this(null, null,null,null,null,null,null);
        }

        public BlogPushAllDtoBuilder id(Long id) {
            return new BlogPushAllDtoBuilder(id, title, description, content, status, link, sensitiveContentList);
        }

        public BlogPushAllDtoBuilder title(String title) {
            return new BlogPushAllDtoBuilder(id, title, description, content, status, link, sensitiveContentList);
        }

        public BlogPushAllDtoBuilder description(String description) {
            return new BlogPushAllDtoBuilder(id, title, description, content, status, link, sensitiveContentList);
        }

        public BlogPushAllDtoBuilder content(String content) {
            return new BlogPushAllDtoBuilder(id, title, description, content, status, link, sensitiveContentList);
        }

        public BlogPushAllDtoBuilder status(Integer status) {
            return new BlogPushAllDtoBuilder(id, title, description, content, status, link, sensitiveContentList);
        }

        public BlogPushAllDtoBuilder link(String link) {
            return new BlogPushAllDtoBuilder(id, title, description, content, status, link, sensitiveContentList);
        }

        public BlogPushAllDtoBuilder sensitiveContentList(List<SensitiveContentDto> sensitiveContentList) {
            return new BlogPushAllDtoBuilder(id, title, description, content, status, link, sensitiveContentList);
        }

        public BlogPushAllDto build() {
            return new BlogPushAllDto(id, title, description, content, status, link, sensitiveContentList);
        }
    }

    // 敏感内容DTO
    public record SensitiveContentDto(
            Integer startIndex,
            Integer endIndex,
            Integer type
    ) implements Serializable {
        public static SensitiveContentDtoBuilder builder() {
            return new SensitiveContentDtoBuilder();
        }

        public record SensitiveContentDtoBuilder(
                Integer startIndex,
                Integer endIndex,
                Integer type
        ) {
            public SensitiveContentDtoBuilder() {
                this(null, null, null);
            }

            public SensitiveContentDtoBuilder startIndex(Integer startIndex) {
                return new SensitiveContentDtoBuilder(startIndex, endIndex, type);
            }

            public SensitiveContentDtoBuilder endIndex(Integer endIndex) {
                return new SensitiveContentDtoBuilder(startIndex, endIndex, type);
            }

            public SensitiveContentDtoBuilder type(Integer type) {
                return new SensitiveContentDtoBuilder(startIndex, endIndex, type);
            }

            public SensitiveContentDto build() {
                return new SensitiveContentDto(startIndex, endIndex, type);
            }
        }
    }
}