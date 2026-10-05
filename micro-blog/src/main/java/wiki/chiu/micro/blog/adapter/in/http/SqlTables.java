package wiki.chiu.micro.blog.adapter.in.http;

import static wiki.chiu.micro.common.constant.Const.BLOG_SENSITIVE_TABLE;
import static wiki.chiu.micro.common.constant.Const.BLOG_TABLE;

import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.SensitiveContent;
import wiki.chiu.micro.common.export.SqlColumn;
import wiki.chiu.micro.common.export.SqlTable;

/**
 * The physical tables and columns of the SQL script the download endpoint delivers, mapped from the
 * exported domain rows.
 */
public final class SqlTables {

    public static final SqlTable<Blog> BLOG =
        SqlTable.of(
            BLOG_TABLE,
            SqlColumn.of("id", Blog::id),
            SqlColumn.of("user_id", Blog::userId),
            SqlColumn.of("title", Blog::title),
            SqlColumn.of("description", Blog::description),
            SqlColumn.of("content", Blog::content),
            SqlColumn.of("created", Blog::created),
            SqlColumn.of("updated", Blog::updated),
            SqlColumn.of("status", Blog::status),
            SqlColumn.of("link", Blog::link),
            SqlColumn.of("read_count", Blog::readCount),
            SqlColumn.of("event_revision", Blog::eventRevision));

    public static final SqlTable<SensitiveContent> BLOG_SENSITIVE =
        SqlTable.of(
            BLOG_SENSITIVE_TABLE,
            SqlColumn.of("id", SensitiveContent::id),
            SqlColumn.of("blog_id", SensitiveContent::blogId),
            SqlColumn.of("start_index", SensitiveContent::startIndex),
            SqlColumn.of("end_index", SensitiveContent::endIndex),
            SqlColumn.of("type", SensitiveContent::type),
            SqlColumn.of("created", SensitiveContent::created),
            SqlColumn.of("updated", SensitiveContent::updated));

    private SqlTables() {
    }
}
