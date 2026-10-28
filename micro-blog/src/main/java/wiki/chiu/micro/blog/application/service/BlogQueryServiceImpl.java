package wiki.chiu.micro.blog.application.service;

import java.util.List;

import wiki.chiu.micro.blog.application.port.in.BlogQueryService;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogWriter;

public class BlogQueryServiceImpl implements BlogQueryService {

    private final BlogQueryStore blogs;
    private final BlogWriter blogWrapper;

    public BlogQueryServiceImpl(BlogQueryStore blogs, BlogWriter blogWrapper) {
        this.blogs = blogs;
        this.blogWrapper = blogWrapper;
    }

    @Override
    public List<Long> findIdsAfter(Long afterId, Integer limit) {
        return blogs.findIdsAfter(afterId, limit);
    }

    @Override
    public void incrementViews(Long blogId) {
        blogWrapper.incrementViews(blogId);
    }

}
