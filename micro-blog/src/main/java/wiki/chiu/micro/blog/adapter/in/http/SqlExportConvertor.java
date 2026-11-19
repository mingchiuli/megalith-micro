package wiki.chiu.micro.blog.adapter.in.http;

import java.util.List;

import wiki.chiu.micro.blog.application.model.BlogExportPage;
import wiki.chiu.micro.blog.application.model.BlogExportSensitive;
import wiki.chiu.micro.common.export.SQLUtils;

/**
 * Renders one export page as the INSERT statements the download endpoint delivers. The script
 * format is delivery vocabulary, so it lives in the adapter that offers the download.
 */
public final class SqlExportConvertor {

    private SqlExportConvertor() {
    }

    public static String toSql(BlogExportPage page) {
        StringBuilder script = new StringBuilder();
        appendStatement(script, SQLUtils.insertSql(page.blogs(), SqlTables.BLOG));
        appendStatement(
            script, SQLUtils.insertSql(page.revisions(), SqlTables.BLOG_REVISION));
        appendStatement(
            script,
            SQLUtils.insertSql(
                sensitiveContent(page), SqlTables.BLOG_REVISION_SENSITIVE));
        return script.toString();
    }

    private static void appendStatement(StringBuilder script, String statement) {
        if (!statement.isBlank()) {
            script.append(statement).append(System.lineSeparator());
        }
    }

    public static List<BlogExportSensitive> sensitiveContent(BlogExportPage page) {
        return page.revisions().stream()
            .flatMap(
                revision ->
                    revision.sensitiveContent().stream()
                        .map(
                            span ->
                                new BlogExportSensitive(
                                    revision.id(),
                                    span.startIndex(),
                                    span.endIndex(),
                                    span.type())))
            .toList();
    }
}
