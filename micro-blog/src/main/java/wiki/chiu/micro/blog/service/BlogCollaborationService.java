package wiki.chiu.micro.blog.service;

import java.util.List;
import wiki.chiu.micro.common.lang.DataPermissionEnum;

public interface BlogCollaborationService {

  String issueReadToken(Long blogId, Long userId, List<DataPermissionEnum> dataPermissions);

  String issueWebSocketTicket(Long blogId, Long userId, List<DataPermissionEnum> dataPermissions);
}
