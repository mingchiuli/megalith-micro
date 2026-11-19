package wiki.chiu.micro.search.adapter.in.http;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;
import static wiki.chiu.micro.common.web.FunctionalWeb.strictBoolean;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import wiki.chiu.micro.common.result.Result;
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.port.in.SearchBlogsUseCase;

/**
 * @author mingchiuli
 * @create 2022-11-30 8:48 pm
 */
@Component
public class BlogSearchHttpHandler {

    private final SearchBlogsUseCase searchBlogs;
    private final ValidatedRequest v;

    public BlogSearchHttpHandler(SearchBlogsUseCase searchBlogs, ValidatedRequest v) {
        this.searchBlogs = searchBlogs;
        this.v = v;
    }

    public ServerResponse searchBlogs(ServerRequest request) {
        Integer currentPage =
            v.positive(requiredParam(request, "currentPage", Integer::valueOf), "currentPage");
        Boolean allInfo = requiredParam(request, "allInfo", value -> strictBoolean(value));
        String keywords = v.size(requiredParam(request, "keywords"), 1, 20, "keywords");
        return ok(
            Result.success(
                () ->
                    SearchResponseConvertor.toResponse(
                        searchBlogs.searchPublic(
                            new PublicBlogSearchQuery(currentPage, keywords, allInfo)))));
    }

}
