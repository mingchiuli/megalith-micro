package wiki.chiu.micro.search.handler;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.search.service.BlogSearchService;
import wiki.chiu.micro.search.vo.BlogDocumentVo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.web.ValidatedRequest;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.positive;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;
import static wiki.chiu.micro.common.web.FunctionalWeb.strictBoolean;


/**
 * @author mingchiuli
 * @create 2022-11-30 8:48 pm
 */
@Component
public class BlogSearchHttpHandler {

    private final BlogSearchService blogSearchService;
    private final ValidatedRequest validation;

    public BlogSearchHttpHandler(BlogSearchService blogSearchService, ValidatedRequest validation) {
        this.blogSearchService = blogSearchService;
        this.validation = validation;
    }

    public ServerResponse searchBlogs(ServerRequest request) {
        Integer currentPage = positive(requiredParam(request, "currentPage", Integer::valueOf), "currentPage");
        Boolean allInfo = requiredParam(request, "allInfo", value -> strictBoolean(value));
        String keywords = validation.size(requiredParam(request, "keywords"), 1, 20, "keywords");
        return ok(Result.success(() -> blogSearchService.selectBlogsByES(currentPage, keywords, allInfo)));
    }

}
