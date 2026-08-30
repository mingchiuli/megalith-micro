package wiki.chiu.micro.search.adapter.in.http;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;
import static wiki.chiu.micro.common.web.FunctionalWeb.strictBoolean;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.model.SearchPage;
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
                toResponse(
                    searchBlogs.searchPublic(
                        new PublicBlogSearchQuery(currentPage, keywords, allInfo)))));
  }

  private static PageAdapter<BlogDocumentVo> toResponse(SearchPage<BlogSearchHit> page) {
    return PageAdapter.<BlogDocumentVo>builder()
        .content(page.content().stream().map(BlogSearchHttpHandler::toResponse).toList())
        .totalElements(page.totalElements())
        .pageNumber(page.pageNumber())
        .pageSize(page.pageSize())
        .first(page.first())
        .last(page.last())
        .empty(page.empty())
        .totalPages(page.totalPages())
        .build();
  }

  private static BlogDocumentVo toResponse(BlogSearchHit hit) {
    return BlogDocumentVo.builder()
        .id(hit.id())
        .userId(hit.userId())
        .status(hit.status())
        .title(hit.title())
        .description(hit.description())
        .content(hit.content())
        .created(hit.created())
        .score(hit.score())
        .highlight(hit.highlight())
        .build();
  }
}
