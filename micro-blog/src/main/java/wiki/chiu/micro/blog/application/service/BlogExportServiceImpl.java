package wiki.chiu.micro.blog.application.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wiki.chiu.micro.blog.application.model.BlogDownloadQuery;
import wiki.chiu.micro.blog.application.model.BlogSearchQuery;
import wiki.chiu.micro.blog.application.model.BlogSearchResult;
import wiki.chiu.micro.blog.application.model.BlogSearchSelection;
import wiki.chiu.micro.blog.application.model.SqlTables;
import wiki.chiu.micro.blog.application.port.in.BlogExportService;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogSearchGateway;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.SensitiveContent;
import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.export.SQLUtils;

public class BlogExportServiceImpl implements BlogExportService {

    private static final int PAGE_SIZE = 20;

    private final BlogQueryStore blogs;

    private final BlogSearchGateway search;

    public BlogExportServiceImpl(BlogQueryStore blogs, BlogSearchGateway search) {
        this.blogs = blogs;
        this.search = search;
    }

    @Override
    public void write(
        BlogDownloadQuery query,
        Long userId,
        List<DataPermissionEnum> dataPermissions,
        OutputStream outputStream) {
        boolean allData = dataPermissions.contains(DataPermissionEnum.BLOG_EXPORT_ALL);
        BlogSearchSelection selection =
            new BlogSearchSelection(
                query.status(), query.createStart(), query.createEnd(), userId, allData);
        long total = search.countBlogs(new BlogSearchQuery(0, 0, query.keywords(), selection));
        long pageCount = (total + PAGE_SIZE - 1) / PAGE_SIZE;

        try {
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            for (int page = 1; page <= pageCount; page++) {
                writePage(
                    search.searchBlogs(new BlogSearchQuery(page, PAGE_SIZE, query.keywords(), selection)),
                    selection,
                    writer);
            }
            writer.flush();
        } catch (IOException exception) {
            throw new IllegalStateException("failed to write blog export", exception);
        }
    }

    private void writePage(
        BlogSearchResult result, BlogSearchSelection selection, OutputStreamWriter writer)
        throws IOException {
        List<Long> ids = result.ids();
        if (ids.isEmpty()) {
            return;
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
        writeStatement(writer, SQLUtils.insertSql(pageBlogs, SqlTables.BLOG));
        writeStatement(writer, SQLUtils.insertSql(pageSensitive, SqlTables.BLOG_SENSITIVE));
    }

    private static void writeStatement(OutputStreamWriter writer, String statement)
        throws IOException {
        if (!statement.isBlank()) {
            writer.write(statement);
            writer.write(System.lineSeparator());
        }
    }
}
