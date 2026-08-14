package wiki.chiu.micro.blog.service;

import java.io.OutputStream;
import java.util.List;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.common.lang.DataPermissionEnum;

public interface BlogExportService {

  void write(
      BlogDownloadReq request,
      Long userId,
      List<DataPermissionEnum> dataPermissions,
      OutputStream outputStream);
}
