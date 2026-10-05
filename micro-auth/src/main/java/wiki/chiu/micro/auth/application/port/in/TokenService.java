package wiki.chiu.micro.auth.application.port.in;

import wiki.chiu.micro.auth.application.model.UserInfo;

/**
 * @author mingchiuli
 * @create 2023-03-30 4:29 am
 */
public interface TokenService {

    String refreshAccessToken(Long userId);

    UserInfo userinfo(Long userId);

    String issueWebSocketTicket(Long userId, String roomId);
}
