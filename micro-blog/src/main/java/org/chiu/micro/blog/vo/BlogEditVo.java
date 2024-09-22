package org.chiu.micro.blog.vo;

import java.util.List;


public record BlogEditVo(

        Long id,

        Long userId,

        String title,

        String description,

        String link,

        String content,

        Integer status,

        Integer version,

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
        private Integer version;
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

        public BlogEditVoBuilder version(Integer version) {
            this.version = version;
            return this;
        }

        public BlogEditVoBuilder sensitiveContentList(List<SensitiveContentVo> sensitiveContentList) {
            this.sensitiveContentList = sensitiveContentList;
            return this;
        }

        public BlogEditVo build() {
            return new BlogEditVo(id, userId, title, description, link, content, status, version, sensitiveContentList);
        }

    }
}
