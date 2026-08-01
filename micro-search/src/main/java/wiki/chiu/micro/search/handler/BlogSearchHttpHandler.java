package wiki.chiu.micro.search.handler;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.search.service.BlogSearchService;
import wiki.chiu.micro.search.vo.BlogDocumentVo;
import org.springframework.stereotype.Component;


/**
 * @author mingchiuli
 * @create 2022-11-30 8:48 pm
 */
@Component
public class BlogSearchHttpHandler {

    private final BlogSearchService blogSearchService;

    public BlogSearchHttpHandler(BlogSearchService blogSearchService) {
        this.blogSearchService = blogSearchService;
    }

    public Result<PageAdapter<BlogDocumentVo>> searchBlogs(Integer currentPage,
                                                           Boolean allInfo,
                                                           String keywords) {
        return Result.success(() -> blogSearchService.selectBlogsByES(currentPage, keywords, allInfo));
    }

}
