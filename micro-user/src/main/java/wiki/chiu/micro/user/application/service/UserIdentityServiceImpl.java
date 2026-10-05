package wiki.chiu.micro.user.application.service;
import static wiki.chiu.micro.common.error.ExceptionMessage.EMAIL_NOT_EXIST;
import static wiki.chiu.micro.common.error.ExceptionMessage.PHONE_NOT_EXIST;
import static wiki.chiu.micro.common.error.ExceptionMessage.USER_MISS;

import java.time.LocalDateTime;


import wiki.chiu.micro.common.enums.StatusEnum;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.user.application.model.UserAccess;
import wiki.chiu.micro.user.domain.User;
import wiki.chiu.micro.user.application.port.in.UserIdentityService;
import wiki.chiu.micro.user.application.port.out.UserIdentityWriter;
import wiki.chiu.micro.user.application.port.out.UserReader;
import wiki.chiu.micro.user.domain.User;

public class UserIdentityServiceImpl implements UserIdentityService {

    private final UserReader users;
    private final UserIdentityWriter identityWrapper;
    private final AuthorizationQueryService authorizationQueries;
    private final int unlockBatchSize;

    public UserIdentityServiceImpl(
        UserReader users,
        UserIdentityWriter identityWrapper,
        AuthorizationQueryService authorizationQueries,
        int unlockBatchSize) {
        this.users = users;
        this.identityWrapper = identityWrapper;
        this.authorizationQueries = authorizationQueries;
        this.unlockBatchSize = unlockBatchSize;
    }

    @Override
    public void updateLoginTime(String username, LocalDateTime time) {
        identityWrapper.updateLoginTime(username, time);
    }

    @Override
    public void lockAfterPasswordFailures(Long userId) {
        identityWrapper.lockAfterPasswordFailures(userId);
    }

    @Override
    public int unlockExpiredBatch() {
        var userIds =
            users.findExpiredPasswordLockIds(
                StatusEnum.HIDE.getCode(), unlockBatchSize);
        return identityWrapper.unlockExpired(userIds);
    }

    @Override
    public User findById(Long userId) {
        User user =
            users.findById(userId).orElseThrow(() -> new MissException(USER_MISS.getMsg()));
        return user;
    }

    @Override
    public UserAccess findUserAccess(Long userId) {
        return authorizationQueries.findUserAccess(userId);
    }

    @Override
    public User findByEmail(String email) {
        User user =
            users.findByEmail(email).orElseThrow(() -> new MissException(EMAIL_NOT_EXIST.getMsg()));
        return user;
    }

    @Override
    public User findByPhone(String phone) {
        User user =
            users.findByPhone(phone).orElseThrow(() -> new MissException(PHONE_NOT_EXIST.getMsg()));
        return user;
    }

    @Override
    public User findByLogin(String login) {
        User user =
            users
                .findByUsernameOrEmailOrPhone(login, login, login)
                .orElseThrow(() -> new MissException(USER_MISS.getMsg()));
        return user;
    }
}
