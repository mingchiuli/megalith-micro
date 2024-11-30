package wiki.chiu.micro.exhibit.wrapper;

import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.common.dto.BlogEntityRpcDto;
import wiki.chiu.micro.common.dto.UserEntityRpcDto;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.exhibit.convertor.BlogDescriptionDtoConvertor;
import wiki.chiu.micro.exhibit.convertor.BlogExhibitDtoConvertor;
import wiki.chiu.micro.exhibit.dto.BlogDescriptionDto;
import wiki.chiu.micro.exhibit.dto.BlogExhibitDto;
import wiki.chiu.micro.exhibit.rpc.BlogHttpServiceWrapper;
import wiki.chiu.micro.exhibit.rpc.SearchHttpServiceWrapper;
import wiki.chiu.micro.exhibit.rpc.UserHttpServiceWrapper;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutorService;


@Component
public class BlogWrapper {

    private final BlogHttpServiceWrapper blogHttpServiceWrapper;

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    private final SearchHttpServiceWrapper searchHttpServiceWrapper;

    private final ExecutorService executorService;

    private final RedissonClient redissonClient;

    @Value("${megalith.blog.blog-page-size}")
    private int blogPageSize;

    public BlogWrapper(BlogHttpServiceWrapper blogHttpServiceWrapper, UserHttpServiceWrapper userHttpServiceWrapper, SearchHttpServiceWrapper searchHttpServiceWrapper, ExecutorService executorService, RedissonClient redissonClient) {
        this.blogHttpServiceWrapper = blogHttpServiceWrapper;
        this.userHttpServiceWrapper = userHttpServiceWrapper;
        this.searchHttpServiceWrapper = searchHttpServiceWrapper;
        this.executorService = executorService;
        this.redissonClient = redissonClient;
    }

    @Cache(prefix = Const.HOT_BLOG)
    public BlogExhibitDto findById(Long id) {
        BlogEntityRpcDto blogEntity = blogHttpServiceWrapper.findById(id);

        UserEntityRpcDto user = userHttpServiceWrapper.findById(blogEntity.userId());
        return BlogExhibitDtoConvertor.convert(blogEntity, user);
    }

    public void setReadCount(Long id) {
        executorService.execute(() -> {
            blogHttpServiceWrapper.setReadCount(id);
            redissonClient.<String>getScoredSortedSet(Const.HOT_READ).addScore(id.toString(), 1);
            searchHttpServiceWrapper.addReadCount(id);
        });
    }

    @Cache(prefix = Const.BLOG_STATUS)
    public Integer findStatusById(Long blogId) {
        return blogHttpServiceWrapper.findStatusById(blogId);
    }

    @Cache(prefix = Const.HOT_BLOGS)
    public PageAdapter<BlogDescriptionDto> findPage(Integer currentPage, Integer year) {
        PageAdapter<BlogEntityRpcDto> page = Objects.equals(year, Integer.MIN_VALUE) ?
                blogHttpServiceWrapper.findPage(currentPage, blogPageSize) :
                blogHttpServiceWrapper.findPageByCreatedBetween(currentPage, blogPageSize,
                        LocalDateTime.of(year, 1, 1, 0, 0, 0),
                        LocalDateTime.of(year, 12, 31, 23, 59, 59));

        return BlogDescriptionDtoConvertor.convert(page);
    }

}
