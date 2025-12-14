package wiki.chiu.micro.exhibit.wrapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
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



@Component
public class BlogWrapper {

    private final BlogHttpServiceWrapper blogHttpServiceWrapper;

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    private final SearchHttpServiceWrapper searchHttpServiceWrapper;

    private final TaskExecutor taskExecutor;

    private final RedissonClient redissonClient;

    @Value("${megalith.blog.blog-page-size}")
    private int blogPageSize;

    public BlogWrapper(BlogHttpServiceWrapper blogHttpServiceWrapper, UserHttpServiceWrapper userHttpServiceWrapper, SearchHttpServiceWrapper searchHttpServiceWrapper, @Qualifier("commonExecutor") TaskExecutor taskExecutor, RedissonClient redissonClient) {
        this.blogHttpServiceWrapper = blogHttpServiceWrapper;
        this.userHttpServiceWrapper = userHttpServiceWrapper;
        this.searchHttpServiceWrapper = searchHttpServiceWrapper;
        this.taskExecutor = taskExecutor;
        this.redissonClient = redissonClient;
    }

    @Cache(prefix = Const.HOT_BLOG)
    public BlogExhibitDto findById(Long id) {
        BlogEntityRpcVo blogEntity = blogHttpServiceWrapper.findById(id);

        UserEntityRpcVo user = userHttpServiceWrapper.findById(blogEntity.userId());
        return BlogExhibitDtoConvertor.convert(blogEntity, user);
    }

    public void setReadCount(Long id) {
        taskExecutor.execute(() -> {
            blogHttpServiceWrapper.setReadCount(id);
            redissonClient.<String>getScoredSortedSet(Const.HOT_READ).addScore(id.toString(), 1);
            searchHttpServiceWrapper.addReadCount(id);
        });
    }

    @Cache(prefix = Const.HOT_BLOGS)
    public PageAdapter<BlogDescriptionDto> findPage(Integer currentPage) {
        PageAdapter<BlogEntityRpcVo> page = blogHttpServiceWrapper.findPage(currentPage, blogPageSize);
        return BlogDescriptionDtoConvertor.convert(page);
    }

}
