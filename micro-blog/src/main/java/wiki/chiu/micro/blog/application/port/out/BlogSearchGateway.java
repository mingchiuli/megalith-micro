package wiki.chiu.micro.blog.application.port.out;

import wiki.chiu.micro.blog.application.model.BlogSearchQuery;
import wiki.chiu.micro.blog.application.model.BlogSearchResult;

public interface BlogSearchGateway {

    BlogSearchResult searchBlogs(BlogSearchQuery query);

    long countBlogs(BlogSearchQuery query);
}
