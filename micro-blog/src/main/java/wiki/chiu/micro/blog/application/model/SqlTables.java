package wiki.chiu.micro.blog.application.model;

import static wiki.chiu.micro.common.constant.Const.BLOG_SENSITIVE_TABLE;
import static wiki.chiu.micro.common.constant.Const.BLOG_TABLE;

import wiki.chiu.micro.blog.domain.BlogEntity;
import wiki.chiu.micro.blog.domain.BlogSensitiveContentEntity;
import wiki.chiu.micro.common.export.SqlColumn;
import wiki.chiu.micro.common.export.SqlTable;

/**
 * Explicit SQL export column definitions for the blog service tables.
 */
public final class SqlTables {

    public static final SqlTable<BlogEntity> BLOG =
        SqlTable.of(
            BLOG_TABLE,
            SqlColumn.of("id", BlogEntity::getId),
            SqlColumn.of("user_id", BlogEntity::getUserId),
            SqlColumn.of("title", BlogEntity::getTitle),
            SqlColumn.of("description", BlogEntity::getDescription),
            SqlColumn.of("content", BlogEntity::getContent),
            SqlColumn.of("created", BlogEntity::getCreated),
            SqlColumn.of("updated", BlogEntity::getUpdated),
            SqlColumn.of("status", BlogEntity::getStatus),
            SqlColumn.of("link", BlogEntity::getLink),
            SqlColumn.of("read_count", BlogEntity::getReadCount),
            SqlColumn.of("event_revision", BlogEntity::getEventRevision));

    public static final SqlTable<BlogSensitiveContentEntity> BLOG_SENSITIVE =
        SqlTable.of(
            BLOG_SENSITIVE_TABLE,
            SqlColumn.of("id", BlogSensitiveContentEntity::getId),
            SqlColumn.of("blog_id", BlogSensitiveContentEntity::getBlogId),
            SqlColumn.of("start_index", BlogSensitiveContentEntity::getStartIndex),
            SqlColumn.of("end_index", BlogSensitiveContentEntity::getEndIndex),
            SqlColumn.of("type", BlogSensitiveContentEntity::getType),
            SqlColumn.of("created", BlogSensitiveContentEntity::getCreated),
            SqlColumn.of("updated", BlogSensitiveContentEntity::getUpdated));

    private SqlTables() {
    }
}
