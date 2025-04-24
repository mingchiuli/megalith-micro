package wiki.chiu.micro.exhibit.wrapper;

import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.common.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.vo.UserEntityRpcVo;
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
        BlogEntityRpcVo blogEntity = blogHttpServiceWrapper.findById(id);

        UserEntityRpcVo user = userHttpServiceWrapper.findById(blogEntity.userId());
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
    public PageAdapter<BlogDescriptionDto> findPage(Integer currentPage) {
        PageAdapter<BlogEntityRpcVo> page = blogHttpServiceWrapper.findPage(currentPage, blogPageSize);
        return BlogDescriptionDtoConvertor.convert(page);
    }

}
