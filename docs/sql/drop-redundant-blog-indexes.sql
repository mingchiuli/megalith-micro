-- Run against the blog database after deploying the updated entity mappings.
-- DDL commits independently. Keep the matching unique indexes; skip cleanup if they are absent.
-- Re-running this script is safe. No application data or unique constraints are removed.

SELECT COUNT(*) INTO @draft_unique_indexes
FROM (
    SELECT index_name
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'm_blog_draft' AND non_unique = 0
    GROUP BY index_name
    HAVING GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ',') = 'blog_id'
) AS matching_indexes;

SET @cleanup_sql = IF(
    @draft_unique_indexes > 0 AND EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'm_blog_draft'
          AND index_name = 'idx_blog_draft_blog' AND non_unique = 1
    ),
    'ALTER TABLE m_blog_draft DROP INDEX idx_blog_draft_blog',
    'SELECT ''Skipped idx_blog_draft_blog: already absent or matching unique index missing'' AS result'
);
PREPARE cleanup_statement FROM @cleanup_sql;
EXECUTE cleanup_statement;
DEALLOCATE PREPARE cleanup_statement;

SELECT COUNT(*) INTO @revision_unique_indexes
FROM (
    SELECT index_name
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'm_blog_revision' AND non_unique = 0
    GROUP BY index_name
    HAVING GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ',') = 'blog_id,version_no'
) AS matching_indexes;

SET @cleanup_sql = IF(
    @revision_unique_indexes > 0 AND EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'm_blog_revision'
          AND index_name = 'idx_blog_revision_blog' AND non_unique = 1
    ),
    'ALTER TABLE m_blog_revision DROP INDEX idx_blog_revision_blog',
    'SELECT ''Skipped idx_blog_revision_blog: already absent or matching unique index missing'' AS result'
);
PREPARE cleanup_statement FROM @cleanup_sql;
EXECUTE cleanup_statement;
DEALLOCATE PREPARE cleanup_statement;
