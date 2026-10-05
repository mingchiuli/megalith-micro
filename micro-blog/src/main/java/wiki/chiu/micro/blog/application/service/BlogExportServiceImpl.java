package wiki.chiu.micro.blog.application.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import wiki.chiu.micro.blog.application.model.BlogDownloadQuery;
import wiki.chiu.micro.blog.application.model.BlogExportPage;
import wiki.chiu.micro.blog.application.model.BlogSearchQuery;
import wiki.chiu.micro.blog.application.model.BlogSearchSelection;
import wiki.chiu.micro.blog.application.port.in.BlogExportService;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogSearchGateway;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.SensitiveContent;
import wiki.chiu.micro.common.enums.DataPermissionEnum;

public class BlogExportServiceImpl implements BlogExportService {

    private static final int PAGE_SIZE = 20;

    private final BlogQueryStore blogs;

    private final BlogSearchGateway search;

    public BlogExportServiceImpl(BlogQueryStore blogs, BlogSearchGateway search) {
        this.blogs = blogs;
        this.search = search;
    }

    @Override
    public Stream<BlogExportPage> pages(
        BlogDownloadQuery query, Long userId, List<DataPermissionEnum> dataPermissions) {
        boolean allData = dataPermissions.contains(DataPermissionEnum.BLOG_EXPORT_ALL);
        BlogSearchSelection selection =
            new BlogSearchSelection(
                query.status(), query.createStart(), query.createEnd(), userId, allData);
        long total = search.countBlogs(new BlogSearchQuery(0, 0, query.keywords(), selection));
        long pageCount = (total + PAGE_SIZE - 1) / PAGE_SIZE;

        return IntStream.rangeClosed(1, Math.toIntExact(pageCount))
            .mapToObj(page -> loadPage(page, query, selection));
    }

    private BlogExportPage loadPage(
        int page, BlogDownloadQuery query, BlogSearchSelection selection) {
        List<Long> ids =
            search.searchBlogs(new BlogSearchQuery(page, PAGE_SIZE, query.keywords(), selection))
                .ids();
        if (ids.isEmpty()) {
            return new BlogExportPage(List.of(), List.of());
        }
        Map<Long, Integer> order = new HashMap<>();
        for (int index = 0; index < ids.size(); index++) {
            order.put(ids.get(index), index);
        }
        List<Blog> pageBlogs =
            blogs.findAllById(ids).stream()
                .filter(selection::includes)
                .sorted(
                    (left, right) ->
                        Integer.compare(
                            order.getOrDefault(left.id(), Integer.MAX_VALUE),
                            order.getOrDefault(right.id(), Integer.MAX_VALUE)))
                .toList();
        List<Long> currentIds = pageBlogs.stream().map(Blog::id).toList();
        List<SensitiveContent> pageSensitive =
            currentIds.isEmpty() ? List.of() : blogs.findSensitiveByBlogIds(currentIds);

        return new BlogExportPage(pageBlogs, pageSensitive);
    }
}
