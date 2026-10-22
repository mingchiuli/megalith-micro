package wiki.chiu.micro.blog.adapter.in.http;

import static wiki.chiu.micro.common.constant.Const.BLOG_REVISION_SENSITIVE_TABLE;
import static wiki.chiu.micro.common.constant.Const.BLOG_REVISION_TABLE;
import static wiki.chiu.micro.common.constant.Const.BLOG_TABLE;

import wiki.chiu.micro.blog.application.model.BlogExportRow;
import wiki.chiu.micro.blog.application.model.BlogExportSensitive;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.common.export.SqlColumn;
import wiki.chiu.micro.common.export.SqlTable;

/**
 * The normalized tables emitted by the SQL download endpoint.
 */
public final class SqlTables {

    public static final SqlTable<BlogExportRow> BLOG =
        SqlTable.of(
            BLOG_TABLE,
            SqlColumn.of("id", row -> row.blog().id()),
            SqlColumn.of("user_id", row -> row.blog().userId()),
            SqlColumn.of("created", row -> row.blog().created()),
            SqlColumn.of("read_count", row -> row.blog().readCount()),
            SqlColumn.of("published_revision_id", row -> row.blog().publishedRevisionId()),
            SqlColumn.of("published_at", row -> row.blog().publishedAt()),
            SqlColumn.of("offline_at", row -> row.blog().offlineAt()),
            SqlColumn.of("access_mode", row -> row.blog().accessMode().getCode()),
            SqlColumn.of("publication_revision", row -> row.blog().publicationRevision()),
            SqlColumn.of("admin_revision", row -> row.blog().adminRevision()));

    public static final SqlTable<BlogVersion> BLOG_REVISION =
        SqlTable.of(
            BLOG_REVISION_TABLE,
            SqlColumn.of("id", BlogVersion::id),
            SqlColumn.of("blog_id", BlogVersion::blogId),
            SqlColumn.of("version_no", BlogVersion::versionNo),
            SqlColumn.of("origin", version -> version.origin().name()),
            SqlColumn.of("base_revision_id", BlogVersion::baseRevisionId),
            SqlColumn.of("title", BlogVersion::title),
            SqlColumn.of("description", BlogVersion::description),
            SqlColumn.of("content", BlogVersion::content),
            SqlColumn.of("link", BlogVersion::link),
            SqlColumn.of("render_mode", version -> version.renderMode().name()),
            SqlColumn.of("first_published_at", BlogVersion::firstPublishedAt),
            SqlColumn.of("created_by", BlogVersion::createdBy),
            SqlColumn.of("created_at", BlogVersion::createdAt));

    public static final SqlTable<BlogExportSensitive> BLOG_REVISION_SENSITIVE =
        SqlTable.of(
            BLOG_REVISION_SENSITIVE_TABLE,
            SqlColumn.of("revision_id", BlogExportSensitive::revisionId),
            SqlColumn.of("start_index", BlogExportSensitive::startIndex),
            SqlColumn.of("end_index", BlogExportSensitive::endIndex),
            SqlColumn.of("type", BlogExportSensitive::type));

    private SqlTables() {
    }
}
