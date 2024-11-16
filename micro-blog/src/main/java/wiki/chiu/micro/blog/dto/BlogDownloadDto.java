package wiki.chiu.micro.blog.dto;

import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.entity.BlogSensitiveContentEntity;

import java.io.Serializable;
import java.util.List;

public record BlogDownloadDto(

        List<BlogEntity> blogs,

        List<BlogSensitiveContentEntity> blogSensitiveContentEntities) implements Serializable {

    public static BlogDownloadDto.BlogEntityDownloadDtoBuilder builder() {
        return new BlogDownloadDto.BlogEntityDownloadDtoBuilder();
    }

    public static class BlogEntityDownloadDtoBuilder {

        private List<BlogEntity> blogs;

        private List<BlogSensitiveContentEntity> blogSensitiveContents;

        public BlogDownloadDto.BlogEntityDownloadDtoBuilder blogs(List<BlogEntity> blogs) {
            this.blogs = blogs;
            return this;
        }

        public BlogDownloadDto.BlogEntityDownloadDtoBuilder blogSensitiveContents(List<BlogSensitiveContentEntity> blogSensitiveContents) {
            this.blogSensitiveContents = blogSensitiveContents;
            return this;
        }

        public BlogDownloadDto build() {
            return new BlogDownloadDto(blogs, blogSensitiveContents);
        }

    }
}
