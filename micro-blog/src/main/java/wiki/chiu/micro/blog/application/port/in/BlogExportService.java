package wiki.chiu.micro.blog.application.port.in;

import java.io.OutputStream;
import java.util.List;

import wiki.chiu.micro.blog.application.model.BlogDownloadQuery;
import wiki.chiu.micro.common.enums.DataPermissionEnum;

public interface BlogExportService {

    void write(
        BlogDownloadQuery query,
        Long userId,
        List<DataPermissionEnum> dataPermissions,
        OutputStream outputStream);
}
