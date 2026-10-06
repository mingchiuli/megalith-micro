package wiki.chiu.micro.blog.application.service;

import static wiki.chiu.micro.common.error.ExceptionMessage.NO_FOUND;

import java.util.List;

import wiki.chiu.micro.blog.application.port.in.BlogQueryService;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogWriter;
import wiki.chiu.micro.blog.application.model.Page;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.model.BlogSnapshot;

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
    public BlogSnapshot findById(Long blogId) {
        return blogs.findById(blogId).orElseThrow(() -> new MissException(NO_FOUND.getMsg())).snapshot();
    }

    @Override
    public List<BlogSnapshot> findAllById(List<Long> ids) {
        return blogs.findAllById(ids).stream().map(Blog::snapshot).toList();
    }

    @Override
    public long count() {
        return blogs.count();
    }

    @Override
    public void incrementViews(Long blogId) {
        blogWrapper.incrementViews(blogId);
    }

    @Override
    public Page<BlogSnapshot> findPage(Integer pageNo, Integer pageSize) {
        List<Integer> statuses =
            List.of(
                BlogStatusEnum.NORMAL.getCode(),
                BlogStatusEnum.SENSITIVE_FILTER.getCode(),
                BlogStatusEnum.HIDE.getCode());
        Page<Blog> page = blogs.findPage(pageNo, pageSize, statuses);
        if (pageNo > 1 && page.empty()) {
            throw new MissException(NO_FOUND.getMsg() + pageNo + " page");
        }
        return page.withContent(page.content().stream().map(Blog::snapshot).toList());
    }
}
