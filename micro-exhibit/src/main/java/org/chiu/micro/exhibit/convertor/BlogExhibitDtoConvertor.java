package org.chiu.micro.exhibit.convertor;

import org.chiu.micro.exhibit.dto.BlogDescriptionDto;
import org.chiu.micro.exhibit.dto.BlogEntityDto;
import org.chiu.micro.exhibit.dto.BlogExhibitDto;
import org.chiu.micro.exhibit.dto.UserEntityDto;

public class BlogExhibitDtoConvertor {

    private BlogExhibitDtoConvertor() {}

    public static BlogExhibitDto convert(BlogEntityDto blogEntity, UserEntityDto user) {
        return BlogExhibitDto.builder()
                .userId(blogEntity.userId())
                .title(blogEntity.title())
                .description(blogEntity.description())
                .content(blogEntity.content())
                .readCount(blogEntity.readCount())
                .nickname(user.nickname())
                .avatar(user.avatar())
                .created(blogEntity.created())
                .readCount(blogEntity.readCount())
                .build();
    }

    public static BlogExhibitDto convert(BlogExhibitDto blog, String title, String description, String content) {
        return BlogExhibitDto.builder()
                .content(content)
                .description(description)
                .title(title)
                .avatar(blog.avatar())
                .created(blog.created())
                .nickname(blog.nickname())
                .readCount(blog.readCount())
                .userId(blog.userId())
                .build();
    }

    public static BlogDescriptionDto convert(BlogDescriptionDto blog, String title, String description) {
        return BlogDescriptionDto.builder()
                .description(description)
                .id(blog.id())
                .link(blog.link())
                .status(blog.status())
                .title(title)
                .created(blog.created())
                .build();
    }
}
