package wiki.chiu.micro.user.service.impl;

import static wiki.chiu.micro.common.lang.ExceptionMessage.EMAIL_NOT_EXIST;
import static wiki.chiu.micro.common.lang.ExceptionMessage.PHONE_NOT_EXIST;
import static wiki.chiu.micro.common.lang.ExceptionMessage.USER_MISS;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;
import wiki.chiu.micro.user.convertor.UserEntityRpcVoConvertor;
import wiki.chiu.micro.user.entity.UserEntity;
import wiki.chiu.micro.user.repository.UserRepository;
import wiki.chiu.micro.user.service.UserIdentityService;
import wiki.chiu.micro.user.wrapper.UserIdentityWrapper;

@Service
public class UserIdentityServiceImpl implements UserIdentityService {

  private final UserRepository users;
  private final UserIdentityWrapper identityWrapper;

  public UserIdentityServiceImpl(UserRepository users, UserIdentityWrapper identityWrapper) {
    this.users = users;
    this.identityWrapper = identityWrapper;
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
  public UserEntityRpcVo findById(Long userId) {
    UserEntity user =
        users.findById(userId).orElseThrow(() -> new MissException(USER_MISS.getMsg()));
    return UserEntityRpcVoConvertor.convert(user);
  }

  @Override
  public UserAccessRpcVo findUserAccess(Long userId) {
    List<UserRepository.UserAccessRow> rows = users.findAccessRows(userId);
    if (rows.isEmpty()) {
      return UserAccessRpcVo.missing(userId);
    }
    UserRepository.UserAccessRow first = rows.getFirst();
    List<Long> roleIds =
        rows.stream()
            .map(UserRepository.UserAccessRow::getRoleId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    return new UserAccessRpcVo(first.getUserId(), true, first.getStatus(), roleIds);
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
