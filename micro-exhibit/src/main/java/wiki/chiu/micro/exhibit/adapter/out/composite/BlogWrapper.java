package wiki.chiu.micro.exhibit.adapter.out.composite;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.search.api.SearchHttpService;
import wiki.chiu.micro.search.api.vo.PublicBlogRpcVo;
import wiki.chiu.micro.exhibit.adapter.out.http.BlogExhibitMapper;
import wiki.chiu.micro.exhibit.adapter.out.http.BlogHttpMapper;
import wiki.chiu.micro.exhibit.adapter.out.http.BlogHttpServiceWrapper;
import wiki.chiu.micro.exhibit.adapter.out.http.UserHttpServiceWrapper;
import wiki.chiu.micro.exhibit.application.model.BlogExhibit;
import wiki.chiu.micro.exhibit.application.model.BlogHotRead;
import wiki.chiu.micro.exhibit.application.model.BlogRecommendation;
import wiki.chiu.micro.exhibit.application.model.Page;
import wiki.chiu.micro.exhibit.application.port.out.BlogReader;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

@Component
public class BlogWrapper implements BlogReader {

    private final SearchHttpService searchHttpService;

    private final BlogHttpServiceWrapper blogHttpServiceWrapper;

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    private final TaskExecutor taskExecutor;

    @Value("${megalith.blog.blog-page-size}")
    private int blogPageSize;

    public BlogWrapper(
        SearchHttpService searchHttpService,
        BlogHttpServiceWrapper blogHttpServiceWrapper,
        UserHttpServiceWrapper userHttpServiceWrapper,
        @Qualifier("commonExecutor") TaskExecutor taskExecutor) {
        this.searchHttpService = searchHttpService;
        this.blogHttpServiceWrapper = blogHttpServiceWrapper;
        this.userHttpServiceWrapper = userHttpServiceWrapper;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public BlogExhibit findById(Long id) {
        PublicBlogRpcVo blogEntity =
            RemoteResult.requireSuccess(() -> searchHttpService.publicBlog(id));

        UserEntityRpcVo user = userHttpServiceWrapper.findById(blogEntity.userId());
        return BlogExhibitMapper.toModel(blogEntity, user);
    }

    @Override
    public void incrementViews(Long id) {
        taskExecutor.execute(() -> blogHttpServiceWrapper.setReadCount(id));
    }

    @Override
    public List<BlogHotRead> hotBlogs(int limit) {
        PageAdapter<PublicBlogRpcVo> page =
            RemoteResult.requireSuccess(
                () -> searchHttpService.recommendationPage(1, limit));
        return page.content().stream().map(BlogHttpMapper::toHotRead).toList();
    }

    @Override
    public Page<BlogRecommendation> recommendationPage(int currentPage) {
        PageAdapter<PublicBlogRpcVo> page =
            RemoteResult.requireSuccess(
                () -> searchHttpService.recommendationPage(currentPage, blogPageSize));
        return BlogHttpMapper.toRecommendationPage(page);
    }
}
