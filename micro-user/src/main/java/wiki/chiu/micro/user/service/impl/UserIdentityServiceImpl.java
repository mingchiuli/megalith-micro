package wiki.chiu.micro.user.service.impl;

import static wiki.chiu.micro.common.lang.ExceptionMessage.EMAIL_NOT_EXIST;
import static wiki.chiu.micro.common.lang.ExceptionMessage.PHONE_NOT_EXIST;
import static wiki.chiu.micro.common.lang.ExceptionMessage.USER_MISS;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.user.api.vo.UserAuthContextRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;
import wiki.chiu.micro.user.convertor.UserEntityRpcVoConvertor;
import wiki.chiu.micro.user.entity.UserEntity;
import wiki.chiu.micro.user.repository.UserRepository;
import wiki.chiu.micro.user.service.UserIdentityService;
import wiki.chiu.micro.user.service.UserRoleService;

@Service
public class UserIdentityServiceImpl implements UserIdentityService {

  private final UserRepository users;
  private final UserRoleService userRoles;

  public UserIdentityServiceImpl(UserRepository users, UserRoleService userRoles) {
    this.users = users;
    this.userRoles = userRoles;
  }

  @Override
  public void updateLoginTime(String username, LocalDateTime time) {
    users.updateLoginTime(username, time);
  }

  @Override
  public void changeStatus(String username, Integer status) {
    users.updateUserStatusByUsername(username, status);
  }

  @Override
  public UserEntityRpcVo findById(Long userId) {
    UserEntity user =
        users.findById(userId).orElseThrow(() -> new MissException(USER_MISS.getMsg()));
    return UserEntityRpcVoConvertor.convert(user);
  }

  @Override
  public UserAuthContextRpcVo findAuthContext(Long userId) {
    UserEntity user =
        users.findById(userId).orElseThrow(() -> new MissException(USER_MISS.getMsg()));
    return new UserAuthContextRpcVo(
        user.getId(), user.getStatus(), userRoles.findRoleCodesByUserId(userId));
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
