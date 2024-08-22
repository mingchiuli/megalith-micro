package org.chiu.micro.exhibit.convertor;

import org.chiu.micro.exhibit.dto.BlogDescriptionDto;
import org.chiu.micro.exhibit.dto.BlogEntityDto;
import org.chiu.micro.exhibit.dto.BlogExhibitDto;
import org.chiu.micro.exhibit.dto.UserEntityDto;

public class BlogExhibitDtoConvertor {

    private BlogExhibitDtoConvertor() {}

    public static BlogExhibitDto convert(BlogEntityDto blogEntity, UserEntityDto user) {
        return BlogExhibitDto.builder()
                .userId(blogEntity.getUserId())
                .title(blogEntity.getTitle())
                .description(blogEntity.getDescription())
                .content(blogEntity.getContent())
                .readCount(blogEntity.getReadCount())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .created(blogEntity.getCreated())
                .readCount(blogEntity.getReadCount())
                .build();
    }

    public static BlogExhibitDto convert(BlogExhibitDto blog, String title, String description, String content) {
        return BlogExhibitDto.builder()
                .content(content)
                .description(description)
                .title(title)
                .avatar(blog.getAvatar())
                .created(blog.getCreated())
                .nickname(blog.getNickname())
                .readCount(blog.getReadCount())
                .userId(blog.getUserId())
                .build();
    }

    public static BlogDescriptionDto convert(BlogDescriptionDto blog, String title, String description) {
        return BlogDescriptionDto.builder()
                .description(description)
                .id(blog.getId())
                .link(blog.getLink())
                .status(blog.getStatus())
                .title(title)
                .created(blog.getCreated())
                .build();
    }
}
