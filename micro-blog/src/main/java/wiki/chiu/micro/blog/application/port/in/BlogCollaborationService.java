package wiki.chiu.micro.blog.application.port.in;

import java.util.List;

import wiki.chiu.micro.blog.application.model.CollaborationTicket;
import wiki.chiu.micro.common.enums.DataPermissionEnum;

public interface BlogCollaborationService {

    String issueReadToken(Long blogId, Long userId, List<DataPermissionEnum> dataPermissions);

    CollaborationTicket issueWebSocketTicket(Long blogId, Long userId, List<DataPermissionEnum> dataPermissions, long expiresAt);
}
