package wiki.chiu.micro.blog.application.service;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

import java.util.List;

import org.springframework.stereotype.Service;

import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.blog.application.port.in.BlogQueryService;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogWriter;
import wiki.chiu.micro.blog.convertor.BlogEntityRpcVoConvertor;
import wiki.chiu.micro.blog.domain.BlogEntity;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.BlogStatusEnum;
import wiki.chiu.micro.common.page.PageAdapter;

@Service
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
    public BlogEntityRpcVo findById(Long blogId) {
        BlogEntity blog =
            blogs.findById(blogId).orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
        return BlogEntityRpcVoConvertor.convert(blog);
    }

    @Override
    public List<BlogEntityRpcVo> findAllById(List<Long> ids) {
        return BlogEntityRpcVoConvertor.convert(blogs.findAllById(ids));
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
    public PageAdapter<BlogEntityRpcVo> findPage(Integer pageNo, Integer pageSize) {
        List<Integer> statuses =
            List.of(
                BlogStatusEnum.NORMAL.getCode(),
                BlogStatusEnum.SENSITIVE_FILTER.getCode(),
                BlogStatusEnum.HIDE.getCode());
        PageAdapter<BlogEntity> page = blogs.findPage(pageNo, pageSize, statuses);
        if (pageNo > 1 && page.empty()) {
            throw new MissException(NO_FOUND.getMsg() + pageNo + " page");
        }
        return BlogEntityRpcVoConvertor.convert(page);
    }

}
