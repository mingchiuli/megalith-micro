package wiki.chiu.micro.user.service.impl;

import static wiki.chiu.micro.common.lang.ExceptionMessage.EMAIL_NOT_EXIST;
import static wiki.chiu.micro.common.lang.ExceptionMessage.PHONE_NOT_EXIST;
import static wiki.chiu.micro.common.lang.ExceptionMessage.USER_MISS;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;
import wiki.chiu.micro.user.config.PasswordLockProperties;
import wiki.chiu.micro.user.convertor.UserEntityRpcVoConvertor;
import wiki.chiu.micro.user.entity.UserEntity;
import wiki.chiu.micro.user.repository.UserRepository;
import wiki.chiu.micro.user.service.AuthorizationQueryService;
import wiki.chiu.micro.user.service.UserIdentityService;
import wiki.chiu.micro.user.wrapper.UserIdentityWrapper;

@Service
public class UserIdentityServiceImpl implements UserIdentityService {

  private final UserRepository users;
  private final UserIdentityWrapper identityWrapper;
  private final AuthorizationQueryService authorizationQueries;
  private final PasswordLockProperties passwordLockProperties;

  public UserIdentityServiceImpl(
      UserRepository users,
      UserIdentityWrapper identityWrapper,
      AuthorizationQueryService authorizationQueries,
      PasswordLockProperties passwordLockProperties) {
    this.users = users;
    this.identityWrapper = identityWrapper;
    this.authorizationQueries = authorizationQueries;
    this.passwordLockProperties = passwordLockProperties;
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
            StatusEnum.HIDE.getCode(),
            org.springframework.data.domain.PageRequest.of(
                0, passwordLockProperties.getBatchSize()));
    return identityWrapper.unlockExpired(userIds);
  }

  @Override
  public UserEntityRpcVo findById(Long userId) {
    UserEntity user =
        users.findById(userId).orElseThrow(() -> new MissException(USER_MISS.getMsg()));
    return UserEntityRpcVoConvertor.convert(user);
  }

  @Override
  public UserAccessRpcVo findUserAccess(Long userId) {
    return authorizationQueries.findUserAccess(userId);
  }

  @Override
  public UserEntityRpcVo findByEmail(String email) {
    UserEntity user =
        users.findByEmail(email).orElseThrow(() -> new MissException(EMAIL_NOT_EXIST.getMsg()));
    return UserEntityRpcVoConvertor.convert(user);
  }

  @Override
  public UserEntityRpcVo findByPhone(String phone) {
    UserEntity user =
        users.findByPhone(phone).orElseThrow(() -> new MissException(PHONE_NOT_EXIST.getMsg()));
    return UserEntityRpcVoConvertor.convert(user);
  }

  @Override
  public UserEntityRpcVo findByLogin(String login) {
    UserEntity user =
        users
            .findByUsernameOrEmailOrPhone(login, login, login)
            .orElseThrow(() -> new MissException(USER_MISS.getMsg()));
    return UserEntityRpcVoConvertor.convert(user);
  }
}
