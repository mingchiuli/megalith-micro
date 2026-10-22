package wiki.chiu.micro.blog.application.port.out;

import wiki.chiu.micro.blog.application.model.BlogSearchQuery;
import wiki.chiu.micro.blog.application.model.BlogSearchResult;
import wiki.chiu.micro.blog.application.model.BlogListItem;
import wiki.chiu.micro.blog.application.model.Page;
import wiki.chiu.micro.blog.application.model.BlogVersionSummary;

public interface BlogSearchGateway {

    BlogSearchResult searchBlogs(BlogSearchQuery query);

    Page<BlogListItem> searchAdmin(BlogSearchQuery query);

    Page<BlogVersionSummary> searchVersions(
        Long blogId, int page, int pageSize, String keywords, long userId, boolean allData);

    long countBlogs(BlogSearchQuery query);
}
