package wiki.chiu.micro.auth.application.service;

import java.util.Objects;

import wiki.chiu.micro.auth.application.model.UserAccount;
import wiki.chiu.micro.auth.application.model.UserInfo;
import wiki.chiu.micro.auth.application.port.in.TokenService;
import wiki.chiu.micro.auth.application.port.out.TokenEncoder;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;
import wiki.chiu.micro.common.enums.StatusEnum;
import wiki.chiu.micro.common.error.ExceptionMessage;
import wiki.chiu.micro.common.exception.MissException;

/**
 * @author mingchiuli
 * @create 2023-03-30 4:29 am
 */
public class TokenServiceImpl implements TokenService {

    private final TokenEncoder tokens;

    private final UserDirectory users;

    public TokenServiceImpl(TokenEncoder tokens, UserDirectory users) {
        this.tokens = tokens;
        this.users = users;
    }

    @Override
    public String refreshAccessToken(Long userId) {
        if (Objects.equals(userId, 0L)) {
            throw new MissException(ExceptionMessage.NO_AUTH);
        }

        if (!StatusEnum.NORMAL.getCode().equals(users.findById(userId).status())) {
            throw new MissException(ExceptionMessage.NO_AUTH);
        }

        return tokens.accessToken(userId);
    }

    @Override
    public UserInfo userinfo(Long userId) {
        UserAccount user = users.findById(userId);
        return new UserInfo(user.id(), user.nickname(), user.avatar());
    }

    @Override
    public String issueWebSocketTicket(Long userId, String roomId) {
        return tokens.webSocketTicket(userId, roomId);
    }
}
