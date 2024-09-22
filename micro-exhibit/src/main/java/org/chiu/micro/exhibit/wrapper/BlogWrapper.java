package org.chiu.micro.exhibit.wrapper;

import org.chiu.micro.exhibit.cache.config.Cache;
import org.chiu.micro.exhibit.convertor.BlogDescriptionDtoConvertor;
import org.chiu.micro.exhibit.convertor.BlogExhibitDtoConvertor;
import org.chiu.micro.exhibit.dto.BlogDescriptionDto;
import org.chiu.micro.exhibit.dto.BlogEntityDto;
import org.chiu.micro.exhibit.dto.BlogExhibitDto;
import org.chiu.micro.exhibit.dto.UserEntityDto;
import org.chiu.micro.exhibit.lang.Const;
import org.chiu.micro.exhibit.page.PageAdapter;
import org.chiu.micro.exhibit.rpc.wrapper.BlogHttpServiceWrapper;
import org.chiu.micro.exhibit.rpc.wrapper.UserhttpServiceWrapper;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutorService;


@Component
public class BlogWrapper {

    private final BlogHttpServiceWrapper blogHttpServiceWrapper;

    private final UserhttpServiceWrapper userHttpServiceWrapper;

    private final ExecutorService executorService;

    private final RedissonClient redissonClient;

    @Value("${blog.blog-page-size}")
    private int blogPageSize;

    public BlogWrapper(BlogHttpServiceWrapper blogHttpServiceWrapper, UserhttpServiceWrapper userHttpServiceWrapper, ExecutorService executorService, RedissonClient redissonClient) {
        this.blogHttpServiceWrapper = blogHttpServiceWrapper;
        this.userHttpServiceWrapper = userHttpServiceWrapper;
        this.executorService = executorService;
        this.redissonClient = redissonClient;
    }

    @Cache(prefix = Const.HOT_BLOG)
    public BlogExhibitDto findById(Long id) {
        BlogEntityDto blogEntity = blogHttpServiceWrapper.findById(id);

        UserEntityDto user = userHttpServiceWrapper.findById(blogEntity.getUserId());
        return BlogExhibitDtoConvertor.convert(blogEntity, user);
    }

    public void setReadCount(Long id) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(servletRequestAttributes, true);//设置子线程共享
        executorService.execute(() -> {
            blogHttpServiceWrapper.setReadCount(id);
            redissonClient.<String>getScoredSortedSet(Const.HOT_READ.getInfo()).addScore(id.toString(), 1);
        });
    }

    @Cache(prefix = Const.BLOG_STATUS)
    public Integer findStatusById(Long blogId) {
        return blogHttpServiceWrapper.findStatusById(blogId);
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
