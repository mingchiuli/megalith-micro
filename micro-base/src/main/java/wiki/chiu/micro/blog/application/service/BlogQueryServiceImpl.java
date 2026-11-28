package wiki.chiu.micro.blog.application.service;

import java.util.List;

import wiki.chiu.micro.blog.application.port.in.BlogQueryService;
import wiki.chiu.micro.blog.application.port.out.BlogAuthoringReader;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogWriter;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.common.error.CommonErrorCode;
import wiki.chiu.micro.common.error.ExceptionMessage;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.exception.MissException;

public class BlogQueryServiceImpl implements BlogQueryService {

    private final BlogQueryStore blogs;
    private final BlogWriter blogWrapper;
    private final BlogAuthoringReader authoring;

    public BlogQueryServiceImpl(BlogQueryStore blogs, BlogWriter blogWrapper, BlogAuthoringReader authoring) {
        this.authoring = authoring;
        this.blogs = blogs;
        this.blogWrapper = blogWrapper;
    }

    @Override
    public BlogIdentity publication(Long blogId) {
        return authoring.findIdentity(blogId).filter(BlogIdentity::online)
            .orElseThrow(() -> new MissException(ExceptionMessage.NO_FOUND));
    }

    @Override
    public BlogVersion publicationContent(Long blogId, Long revisionId) {
        BlogVersion current = authoring.findOnlineVersion(blogId)
            .orElseThrow(() -> new MissException(ExceptionMessage.NO_FOUND));
        if (!current.id().equals(revisionId)) {
            throw new BaseException(CommonErrorCode.CONFLICT, "published version changed");
        }
        return current;
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
