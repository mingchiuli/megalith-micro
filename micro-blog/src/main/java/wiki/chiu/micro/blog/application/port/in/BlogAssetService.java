package wiki.chiu.micro.blog.application.port.in;

import java.util.List;
import wiki.chiu.micro.blog.application.model.UploadObject;
import wiki.chiu.micro.common.lang.DataPermissionEnum;

public interface BlogAssetService {

  String upload(
      UploadObject upload, Long blogId, Long userId, List<DataPermissionEnum> dataPermissions);

  void delete(String url, Long blogId, Long userId, List<DataPermissionEnum> dataPermissions);
}
