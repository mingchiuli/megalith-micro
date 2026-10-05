package wiki.chiu.micro.blog.application.port.in;

import java.util.List;
import java.util.stream.Stream;

import wiki.chiu.micro.blog.application.model.BlogDownloadQuery;
import wiki.chiu.micro.blog.application.model.BlogExportPage;
import wiki.chiu.micro.common.enums.DataPermissionEnum;

public interface BlogExportService {

    /**
     * Yields the export page by page, so a large corpus is never held in memory at once. Every page
     * is fetched while the stream is consumed, so the caller has to close the stream.
     */
    Stream<BlogExportPage> pages(
        BlogDownloadQuery query, Long userId, List<DataPermissionEnum> dataPermissions);
}
