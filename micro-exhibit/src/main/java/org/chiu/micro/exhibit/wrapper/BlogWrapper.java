package org.chiu.micro.exhibit.wrapper;

import lombok.RequiredArgsConstructor;

import org.chiu.micro.exhibit.convertor.BlogDescriptionDtoConvertor;
import org.chiu.micro.exhibit.convertor.BlogExhibitDtoConvertor;
import org.chiu.micro.exhibit.dto.BlogDescriptionDto;
import org.chiu.micro.exhibit.dto.BlogEntityDto;
import org.chiu.micro.exhibit.dto.BlogExhibitDto;
import org.chiu.micro.exhibit.dto.UserEntityDto;
import org.chiu.micro.exhibit.cache.config.Cache;
import org.chiu.micro.exhibit.lang.Const;
import org.chiu.micro.exhibit.lang.StatusEnum;
import org.chiu.micro.exhibit.page.PageAdapter;
import org.chiu.micro.exhibit.rpc.wrapper.BlogHttpServiceWrapper;
import org.chiu.micro.exhibit.rpc.wrapper.UserhttpServiceWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class BlogWrapper {

    private final BlogHttpServiceWrapper blogHttpServiceWrapper;

    private final UserhttpServiceWrapper userHttpServiceWrapper;

    private final StringRedisTemplate redisTemplate;

    @Value("${blog.blog-page-size}")
    private int blogPageSize;

    @Cache(prefix = Const.HOT_BLOG)
    public BlogExhibitDto findById(Long id) {
        BlogEntityDto blogEntity = blogHttpServiceWrapper.findById(id);

        UserEntityDto user = userHttpServiceWrapper.findById(blogEntity.getUserId());
        return BlogExhibitDtoConvertor.convert(blogEntity, user);
    }

    @Async("commonExecutor")
    public void setReadCount(Long id) {
        blogHttpServiceWrapper.setReadCount(id);
        redisTemplate.opsForZSet().incrementScore(Const.HOT_READ.getInfo(), id.toString(), 1);
    }

    @Cache(prefix = Const.BLOG_STATUS)
    public Integer findStatusById(Long blogId) {
        Integer status = blogHttpServiceWrapper.findStatusById(blogId);
        status = Optional.ofNullable(status).orElse(StatusEnum.NORMAL.getCode());
        return status;
    }

    @Cache(prefix = Const.HOT_BLOGS)
    public PageAdapter<BlogDescriptionDto> findPage(Integer currentPage, Integer year) {
        PageAdapter<BlogEntityDto> page = Objects.equals(year, Integer.MIN_VALUE) ?
                blogHttpServiceWrapper.findPage(currentPage, blogPageSize) :
                blogHttpServiceWrapper.findPageByCreatedBetween(currentPage, blogPageSize,
                        LocalDateTime.of(year, 1, 1, 0, 0, 0),
                        LocalDateTime.of(year, 12, 31, 23, 59, 59));

        return BlogDescriptionDtoConvertor.convert(page);
    }

    @Cache(prefix = Const.HOT_BLOG)
    public Long getCountByYear(Integer year) {
        return blogHttpServiceWrapper.countByCreatedBetween(LocalDateTime.of(year, 1, 1, 0, 0, 0),
                LocalDateTime.of(year, 12, 31, 23, 59, 59));
    }

}
