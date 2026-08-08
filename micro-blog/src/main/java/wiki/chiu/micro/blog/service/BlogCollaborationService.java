package wiki.chiu.micro.blog.service;

import java.util.List;

public interface BlogCollaborationService {

  String issueReadToken(Long blogId, Long userId, List<String> roles);

  String issueWebSocketTicket(Long blogId, Long userId, List<String> roles);
}
