package wiki.chiu.micro.exhibit.handler;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.cache.annotation.Checker;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;
import wiki.chiu.micro.exhibit.checker.handler.DetailHandler;
import wiki.chiu.micro.exhibit.checker.handler.ListPageHandler;
import wiki.chiu.micro.exhibit.service.BlogService;
import wiki.chiu.micro.exhibit.vo.BlogDescriptionVo;
import wiki.chiu.micro.exhibit.vo.BlogExhibitVo;
import wiki.chiu.micro.exhibit.vo.BlogHotReadVo;
import wiki.chiu.micro.exhibit.vo.VisitStatisticsVo;

import java.util.List;

@Component
public class BlogExhibitHttpHandler {

    private final BlogService blogService;

    public BlogExhibitHttpHandler(BlogService blogService) {
        this.blogService = blogService;
    }

    @Checker(handler = DetailHandler.class)
    public Result<BlogExhibitVo> getBlogDetail(Long blogId, AuthInfo authInfo) {
        return Result.success(() -> blogService.getBlogDetail(authInfo.roles(), blogId, authInfo.userId()));
    }

    @Checker(handler = ListPageHandler.class)
    public Result<PageAdapter<BlogDescriptionVo>> getPage(Integer currentPage) {
        return Result.success(() -> blogService.findPage(currentPage));
    }

    @Checker(handler = DetailHandler.class)
    public Result<BlogExhibitVo> getLockedBlog(Long blogId, String token) {
        return Result.success(blogService.getLockedBlog(blogId, token));
    }

    @Checker(handler = DetailHandler.class)
    public Result<Boolean> checkReadToken(Long blogId, String token) {
        return Result.success(() -> blogService.checkToken(blogId, token));
    }

    public Result<VisitStatisticsVo> getVisitStatistics() {
        return Result.success(blogService::getVisitStatistics);
    }

    public Result<List<BlogHotReadVo>> getScoreBlogs() {
        return Result.success(blogService::getScoreBlogs);
    }
}
