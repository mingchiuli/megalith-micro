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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.exhibit.application.model.BlogDescription;
import wiki.chiu.micro.exhibit.application.model.BlogExhibit;
import wiki.chiu.micro.exhibit.application.model.BlogHotRead;
import wiki.chiu.micro.exhibit.application.model.BlogScore;
import wiki.chiu.micro.exhibit.application.model.BlogSummary;
import wiki.chiu.micro.exhibit.application.model.VisitStatistics;
import wiki.chiu.micro.exhibit.application.port.in.BlogService;
import wiki.chiu.micro.exhibit.application.port.out.BlogCatalog;
import wiki.chiu.micro.exhibit.application.port.out.BlogReader;
import wiki.chiu.micro.exhibit.application.port.out.ExhibitMetrics;
import wiki.chiu.micro.exhibit.application.port.out.SensitiveContentReader;
import wiki.chiu.micro.exhibit.domain.SensitiveContentMasker;
import wiki.chiu.micro.exhibit.domain.SensitiveSpan;

/**
 * @author mingchiuli
 * @create 2022-11-27 2:10 pm
 */
public class BlogServiceImpl implements BlogService {

    private static final String UNKNOWN_TITLE = "未知标题";

    private static final int HOT_READ_LIMIT = 5;

    private final SensitiveContentReader sensitiveContentReader;

    private final BlogCatalog blogCatalog;

    private final BlogReader blogReader;

    private final ExhibitMetrics metrics;

    public BlogServiceImpl(
        SensitiveContentReader sensitiveContentReader,
        BlogCatalog blogCatalog,
        BlogReader blogReader,
        ExhibitMetrics metrics) {
        this.sensitiveContentReader = sensitiveContentReader;
        this.blogCatalog = blogCatalog;
        this.blogReader = blogReader;
        this.metrics = metrics;
    }

    @Override
    public PageAdapter<BlogDescription> findPage(Integer currentPage) {
        PageAdapter<BlogDescription> page = blogReader.findPage(currentPage);
        List<BlogDescription> masked = page.content().stream().map(this::mask).toList();
        return new PageAdapter<>(masked, page);
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
        List<BlogScore> scores = metrics.topReadBlogs(HOT_READ_LIMIT);
        List<BlogSummary> blogs =
            blogCatalog.findAllById(scores.stream().map(BlogScore::blogId).toList());

        Map<Long, String> titles =
            blogs.stream().collect(Collectors.toMap(BlogSummary::id, BlogSummary::title));
        Set<Long> visibleIds =
            blogs.stream()
                .filter(blog -> !HIDE.getCode().equals(blog.status()))
                .map(BlogSummary::id)
                .collect(Collectors.toSet());

        return scores.stream()
            .filter(score -> visibleIds.contains(score.blogId()))
            .map(
                score ->
                    new BlogHotRead(
                        score.blogId(),
                        titles.getOrDefault(score.blogId(), UNKNOWN_TITLE),
                        score.readCount()))
            .toList();
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
        List<SensitiveSpan> spans = sensitiveContentReader.findSensitiveSpans(description.id());
        if (spans.isEmpty()) {
            return description;
        }
        return description.withMasked(
            SensitiveContentMasker.mask(description.title(), spans, TITLE),
            SensitiveContentMasker.mask(description.description(), spans, DESCRIPTION));
    }

    private BlogExhibit mask(BlogExhibit blog, Long blogId) {
        List<SensitiveSpan> spans = sensitiveContentReader.findSensitiveSpans(blogId);
        if (spans.isEmpty()) {
            return blog;
        }
        return blog.withMasked(
            SensitiveContentMasker.mask(blog.title(), spans, TITLE),
            SensitiveContentMasker.mask(blog.description(), spans, DESCRIPTION),
            SensitiveContentMasker.mask(blog.content(), spans, CONTENT));
    }
}
