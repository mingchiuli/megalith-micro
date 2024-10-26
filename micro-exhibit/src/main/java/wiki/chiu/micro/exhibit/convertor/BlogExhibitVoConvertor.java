package wiki.chiu.micro.exhibit.convertor;

import wiki.chiu.micro.exhibit.dto.BlogExhibitDto;
import wiki.chiu.micro.exhibit.vo.BlogExhibitVo;

public class BlogExhibitVoConvertor {

    private BlogExhibitVoConvertor() {}

    public static BlogExhibitVo convert(BlogExhibitDto dto) {
        return BlogExhibitVo.builder()
                .title(dto.title())
                .description(dto.description())
                .content(dto.content())
                .readCount(dto.readCount())
                .nickname(dto.nickname())
                .avatar(dto.avatar())
                .created(dto.created())
                .readCount(dto.readCount())
                .build();
    }
}
