package wiki.chiu.micro.blog.application.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import wiki.chiu.micro.blog.application.model.BlogDownloadQuery;
import wiki.chiu.micro.blog.application.model.BlogExportPage;
import wiki.chiu.micro.blog.application.model.BlogExportRow;
import wiki.chiu.micro.blog.application.model.BlogListItem;
import wiki.chiu.micro.blog.application.model.BlogSearchQuery;
import wiki.chiu.micro.blog.application.model.BlogSearchSelection;
import wiki.chiu.micro.blog.application.model.Page;
import wiki.chiu.micro.blog.application.port.in.BlogExportService;
import wiki.chiu.micro.blog.application.port.out.BlogAuthoringReader;
import wiki.chiu.micro.blog.application.port.out.BlogSearchGateway;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.common.enums.DataPermissionEnum;

public class BlogExportServiceImpl implements BlogExportService {

    private static final int PAGE_SIZE = 20;

    private final BlogSearchGateway search;
    private final BlogAuthoringReader authoring;

    public BlogExportServiceImpl(BlogSearchGateway search, BlogAuthoringReader authoring) {
        this.search = search;
        this.authoring = authoring;
    }

    @Override
    public Stream<BlogExportPage> pages(
        BlogDownloadQuery query, Long userId, List<DataPermissionEnum> dataPermissions) {
        boolean allData = dataPermissions.contains(DataPermissionEnum.BLOG_EXPORT_ALL);
        BlogSearchSelection selection =
            new BlogSearchSelection(
                query.status(), query.createStart(), query.createEnd(), userId, allData);
        Page<BlogListItem> first =
            search.searchAdmin(new BlogSearchQuery(1, PAGE_SIZE, query.keywords(), selection));

        return IntStream.rangeClosed(1, first.totalPages())
            .mapToObj(
                page ->
                    page == 1
                        ? loadPage(first, selection)
                        : loadPage(
                            search.searchAdmin(
                                new BlogSearchQuery(
                                    page, PAGE_SIZE, query.keywords(), selection)),
                            selection));
    }

    private BlogExportPage loadPage(
        Page<BlogListItem> page, BlogSearchSelection selection) {
        List<Long> ids = page.content().stream().map(BlogListItem::id).toList();
        Map<Long, BlogIdentity> identities = authoring.findIdentities(ids).stream()
            .filter(identity -> selection.allData() || identity.userId().equals(selection.userId()))
            .collect(Collectors.toMap(BlogIdentity::id, Function.identity()));
        List<Long> allowedIds = ids.stream().filter(identities::containsKey).distinct().toList();
        Map<Long, List<BlogVersion>> versions = allowedIds.isEmpty() ? Map.of()
            : authoring.findVersionsByBlogIds(allowedIds).stream()
                .collect(Collectors.groupingBy(BlogVersion::blogId));
        List<BlogExportRow> rows = allowedIds.stream()
            .map(id -> new BlogExportRow(identities.get(id), versions.getOrDefault(id, List.of())))
            .toList();
        return new BlogExportPage(rows);
    }
}
