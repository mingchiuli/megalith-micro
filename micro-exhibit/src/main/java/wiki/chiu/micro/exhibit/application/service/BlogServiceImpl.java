package wiki.chiu.micro.exhibit.application.service;

import static wiki.chiu.micro.common.enums.BlogStatusEnum.DRAFT;
import static wiki.chiu.micro.common.enums.BlogStatusEnum.HIDE;
import static wiki.chiu.micro.common.enums.BlogStatusEnum.SENSITIVE_FILTER;
import static wiki.chiu.micro.common.enums.DataPermissionEnum.BLOG_VIEW_ALL;
import static wiki.chiu.micro.common.enums.SensitiveTypeEnum.CONTENT;
import static wiki.chiu.micro.common.enums.SensitiveTypeEnum.DESCRIPTION;
import static wiki.chiu.micro.common.enums.SensitiveTypeEnum.TITLE;
import static wiki.chiu.micro.common.error.ExceptionMessage.AUTH_EXCEPTION;
import static wiki.chiu.micro.common.error.ExceptionMessage.TOKEN_INVALID;

import java.util.List;
import java.util.Objects;

import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.enums.SensitiveTypeEnum;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.exhibit.application.model.BlogDescription;
import wiki.chiu.micro.exhibit.application.model.BlogExhibit;
import wiki.chiu.micro.exhibit.application.model.BlogHotRead;
import wiki.chiu.micro.exhibit.application.model.BlogRecommendations;
import wiki.chiu.micro.exhibit.application.model.Page;
import wiki.chiu.micro.exhibit.application.model.VisitStatistics;
import wiki.chiu.micro.exhibit.application.port.in.BlogService;
import wiki.chiu.micro.exhibit.application.port.out.BlogReader;
import wiki.chiu.micro.exhibit.application.port.out.ExhibitMetrics;
import wiki.chiu.micro.exhibit.domain.SensitiveContentMasker;
import wiki.chiu.micro.exhibit.domain.SensitiveSpan;

/**
 * @author mingchiuli
 * @create 2022-11-27 2:10 pm
 */
public class BlogServiceImpl implements BlogService {

    private static final int HOT_READ_LIMIT = 5;

    private final BlogReader blogReader;

    private final ExhibitMetrics metrics;

    public BlogServiceImpl(
        BlogReader blogReader,
        ExhibitMetrics metrics) {
        this.blogReader = blogReader;
        this.metrics = metrics;
    }

    @Override
    public Page<BlogDescription> findPage(Integer currentPage) {
        Page<BlogDescription> page = blogReader.findPage(currentPage);
        List<BlogDescription> masked = page.content().stream().map(this::mask).toList();
        return page.withContent(masked);
    }

    @Override
    public BlogExhibit getLockedBlog(Long blogId, String token) {
        String normalizedToken = token.trim();
        if (normalizedToken.isEmpty() || !metrics.consumeReadToken(blogId, normalizedToken)) {
            throw new MissException(TOKEN_INVALID.getMsg());
        }

        blogReader.incrementViews(blogId);
        return blogReader.findById(blogId);
    }

    @Override
    public VisitStatistics getVisitStatistics() {
        List<Long> counts = metrics.visitCounts();
        return new VisitStatistics(counts.get(0), counts.get(1), counts.get(2), counts.get(3));
    }

    @Override
    public List<BlogHotRead> getScoreBlogs() {
        return getRecommendations(null, HOT_READ_LIMIT).hot().stream()
            .map(item -> new BlogHotRead(item.id(), item.title(), item.readCount()))
            .toList();
    }

    @Override
    public BlogRecommendations getRecommendations(Long blogId, int limit) {
        return blogReader.recommendations(blogId, limit);
    }

    @Override
    public BlogExhibit getBlogDetail(
        List<DataPermissionEnum> dataPermissions, Long id, Long userId) {

        BlogExhibit blog = blogReader.findById(id);
        Integer status = blog.status();
        boolean privileged = dataPermissions.contains(BLOG_VIEW_ALL);
        boolean owner = Objects.equals(userId, blog.userId());

        if (HIDE.getCode().equals(status) && !privileged && !owner) {
            throw new MissException(AUTH_EXCEPTION.getMsg());
        }

        if (DRAFT.getCode().equals(status) && Objects.equals(userId, 0L)) {
            throw new MissException(AUTH_EXCEPTION.getMsg());
        }

        BlogExhibit result = blog;
        if (SENSITIVE_FILTER.getCode().equals(status) && !privileged && !owner) {
            result = mask(blog, id);
        }

        blogReader.incrementViews(id);
        return result;
    }

    private BlogDescription mask(BlogDescription description) {
        if (!SENSITIVE_FILTER.getCode().equals(description.status())) {
            return description;
        }
        List<SensitiveSpan> spans =
            description.sensitiveContent().stream()
                .flatMap(
                    span ->
                        SensitiveTypeEnum.ofCode(span.type()).stream()
                            .map(
                                type ->
                                    new SensitiveSpan(
                                        type, span.startIndex(), span.endIndex())))
                .toList();
        if (spans.isEmpty()) {
            return description;
        }
        return description.withMasked(
            SensitiveContentMasker.mask(description.title(), spans, TITLE),
            SensitiveContentMasker.mask(description.description(), spans, DESCRIPTION));
    }

    private BlogExhibit mask(BlogExhibit blog, Long blogId) {
        List<SensitiveSpan> spans =
            blog.sensitiveContent().stream()
                .flatMap(
                    span ->
                        SensitiveTypeEnum.ofCode(span.type()).stream()
                            .map(
                                type ->
                                    new SensitiveSpan(
                                        type, span.startIndex(), span.endIndex())))
                .toList();
        if (spans.isEmpty()) {
            return blog;
        }
        return blog.withMasked(
            SensitiveContentMasker.mask(blog.title(), spans, TITLE),
            SensitiveContentMasker.mask(blog.description(), spans, DESCRIPTION),
            SensitiveContentMasker.mask(blog.content(), spans, CONTENT));
    }
}
