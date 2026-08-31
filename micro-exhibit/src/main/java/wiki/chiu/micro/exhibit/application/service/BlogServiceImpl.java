package wiki.chiu.micro.exhibit.application.service;

import static wiki.chiu.micro.common.lang.ExceptionMessage.AUTH_EXCEPTION;
import static wiki.chiu.micro.common.lang.ExceptionMessage.TOKEN_INVALID;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.blog.api.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.blog.api.vo.SensitiveContentRpcVo;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.BlogStatusEnum;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.exhibit.application.port.in.BlogService;
import wiki.chiu.micro.exhibit.application.port.out.BlogCatalog;
import wiki.chiu.micro.exhibit.application.port.out.BlogReader;
import wiki.chiu.micro.exhibit.application.port.out.ExhibitMetrics;
import wiki.chiu.micro.exhibit.application.port.out.SensitiveContentReader;
import wiki.chiu.micro.exhibit.convertor.BlogDescriptionVoConvertor;
import wiki.chiu.micro.exhibit.convertor.BlogExhibitVoConvertor;
import wiki.chiu.micro.exhibit.convertor.BlogHotReadVoConvertor;
import wiki.chiu.micro.exhibit.convertor.VisitStatisticsVoConvertor;
import wiki.chiu.micro.exhibit.dto.*;
import wiki.chiu.micro.exhibit.utils.SensitiveUtils;
import wiki.chiu.micro.exhibit.vo.BlogDescriptionVo;
import wiki.chiu.micro.exhibit.vo.BlogExhibitVo;
import wiki.chiu.micro.exhibit.vo.BlogHotReadVo;
import wiki.chiu.micro.exhibit.vo.VisitStatisticsVo;

/**
 * @author mingchiuli
 * @create 2022-11-27 2:10 pm
 */
@Service
public class BlogServiceImpl implements BlogService {

    private final SensitiveContentReader blogSensitiveWrapper;

    private final BlogCatalog blogCatalog;

    private final BlogReader blogWrapper;
    private final ExhibitMetrics metrics;

    public BlogServiceImpl(
        SensitiveContentReader blogSensitiveWrapper,
        BlogCatalog blogCatalog,
        BlogReader blogWrapper,
        ExhibitMetrics metrics) {
        this.blogSensitiveWrapper = blogSensitiveWrapper;
        this.blogCatalog = blogCatalog;
        this.blogWrapper = blogWrapper;
        this.metrics = metrics;
    }

    @Override
    public PageAdapter<BlogDescriptionVo> findPage(Integer currentPage) {
        PageAdapter<BlogDescriptionDto> dtoPageAdapter = blogWrapper.findPage(currentPage);
        List<BlogDescriptionDto> descList = dtoPageAdapter.content();

        List<BlogDescriptionDto> descSensitiveList =
            descList.stream().map(this::processSensitiveContent).toList();

        var pageAdapter = new PageAdapter<>(descSensitiveList, dtoPageAdapter);
        return BlogDescriptionVoConvertor.convert(pageAdapter);
    }

    private BlogDescriptionDto processSensitiveContent(BlogDescriptionDto desc) {
        if (!BlogStatusEnum.SENSITIVE_FILTER.getCode().equals(desc.status())) {
            return desc;
        }
        List<SensitiveContentRpcVo> words =
            blogSensitiveWrapper.findSensitiveByBlogId(desc.id()).sensitiveContent();
        if (words.isEmpty()) {
            return desc;
        }
        return SensitiveUtils.deal(words, desc);
    }

    @Override
    public BlogExhibitVo getLockedBlog(Long blogId, String token) {
        String normalizedToken = token.trim();
        if (!StringUtils.hasLength(normalizedToken)
            || !metrics.consumeReadToken(blogId, normalizedToken)) {
            throw new MissException(TOKEN_INVALID.getMsg());
        }

        blogWrapper.incrementViews(blogId);
        BlogExhibitDto blogExhibitDto = blogWrapper.findById(blogId);
        return BlogExhibitVoConvertor.convert(blogExhibitDto);
    }

    @Override
    public VisitStatisticsVo getVisitStatistics() {
        return VisitStatisticsVoConvertor.convert(metrics.visitCounts());
    }

    @Override
    public List<BlogHotReadVo> getScoreBlogs() {
        var scores = metrics.topReadBlogs(5);
        List<Long> ids = scores.stream().map(item -> item.blogId()).toList();

        List<BlogEntityRpcVo> blogs = blogCatalog.findAllById(ids);

        return BlogHotReadVoConvertor.convert(blogs, scores);
    }

    @Override
    public BlogExhibitVo getBlogDetail(
        List<DataPermissionEnum> dataPermissions, Long id, Long userId) {

        BlogExhibitDto rawBlog = blogWrapper.findById(id);
        Integer status = rawBlog.status();

        if (BlogStatusEnum.HIDE.getCode().equals(status)
            && !dataPermissions.contains(DataPermissionEnum.BLOG_VIEW_ALL)
            && !Objects.equals(userId, rawBlog.userId())) {
            throw new MissException(AUTH_EXCEPTION.getMsg());
        }

        if (BlogStatusEnum.DRAFT.getCode().equals(status) && Objects.equals(userId, 0L)) {
            throw new MissException(AUTH_EXCEPTION.getMsg());
        }

        if (BlogStatusEnum.SENSITIVE_FILTER.getCode().equals(status)
            && !dataPermissions.contains(DataPermissionEnum.BLOG_VIEW_ALL)
            && !Objects.equals(userId, rawBlog.userId())) {
            BlogSensitiveContentRpcVo sensitiveContentDto =
                blogSensitiveWrapper.findSensitiveByBlogId(id);
            List<SensitiveContentRpcVo> words = sensitiveContentDto.sensitiveContent();
            if (!words.isEmpty()) {
                BlogExhibitDto dealBlog = SensitiveUtils.deal(words, rawBlog);
                blogWrapper.incrementViews(id);
                return BlogExhibitVoConvertor.convert(dealBlog);
            }
        }

        blogWrapper.incrementViews(id);
        return BlogExhibitVoConvertor.convert(rawBlog);
    }
}
