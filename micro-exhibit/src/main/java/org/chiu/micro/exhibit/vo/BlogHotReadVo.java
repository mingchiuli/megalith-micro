package org.chiu.micro.exhibit.vo;

/**
 * @author mingchiuli
 * @create 2023-03-30 3:15 am
 */
public record BlogHotReadVo(

        Long id,

        String title,

        Long readCount) {

    public static BlogHotReadVoBuilder builder() {
        return new BlogHotReadVoBuilder();
    }

    public static class BlogHotReadVoBuilder {
        private Long id;
        private String title;
        private Long readCount;


        public BlogHotReadVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogHotReadVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogHotReadVoBuilder readCount(Long readCount) {
            this.readCount = readCount;
            return this;
        }

        public BlogHotReadVo build() {
            return new BlogHotReadVo(id, title, readCount);
        }

    }
}
