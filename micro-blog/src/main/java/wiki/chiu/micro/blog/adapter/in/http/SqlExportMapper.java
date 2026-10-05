package wiki.chiu.micro.blog.adapter.in.http;

import wiki.chiu.micro.blog.application.model.BlogExportPage;
import wiki.chiu.micro.common.export.SQLUtils;

/**
 * Renders one export page as the INSERT statements the download endpoint delivers. The script
 * format is delivery vocabulary, so it lives in the adapter that offers the download.
 */
public final class SqlExportMapper {

    private SqlExportMapper() {
    }

    public static String toSql(BlogExportPage page) {
        StringBuilder script = new StringBuilder();
        appendStatement(script, SQLUtils.insertSql(page.blogs(), SqlTables.BLOG));
        appendStatement(
            script, SQLUtils.insertSql(page.sensitiveContent(), SqlTables.BLOG_SENSITIVE));
        return script.toString();
    }

    private static void appendStatement(StringBuilder script, String statement) {
        if (!statement.isBlank()) {
            script.append(statement).append(System.lineSeparator());
        }
    }
}
