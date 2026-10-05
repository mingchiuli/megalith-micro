package wiki.chiu.micro.blog.application.service;

import static wiki.chiu.micro.common.error.ExceptionMessage.NO_FOUND;

import java.util.List;

import wiki.chiu.micro.blog.application.port.in.BlogQueryService;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogWriter;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.common.page.PageAdapter;

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
    public PageAdapter<BlogSnapshot> findPage(Integer pageNo, Integer pageSize) {
        List<Integer> statuses =
            List.of(
                BlogStatusEnum.NORMAL.getCode(),
                BlogStatusEnum.SENSITIVE_FILTER.getCode(),
                BlogStatusEnum.HIDE.getCode());
        PageAdapter<Blog> page = blogs.findPage(pageNo, pageSize, statuses);
        if (pageNo > 1 && page.empty()) {
            throw new MissException(NO_FOUND.getMsg() + pageNo + " page");
        }
        return PageAdapter.<BlogSnapshot>builder()
            .content(page.content().stream().map(Blog::snapshot).toList())
            .totalElements(page.totalElements())
            .pageNumber(page.pageNumber())
            .pageSize(page.pageSize())
            .first(page.first())
            .last(page.last())
            .empty(page.empty())
            .totalPages(page.totalPages())
            .build();
    }
}
