package wiki.chiu.micro.user.service.impl;

import static wiki.chiu.micro.common.lang.Const.USER;
import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_AUTH;
import static wiki.chiu.micro.common.lang.StatusEnum.NORMAL;

import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.exception.ValidationException;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.repository.UserRepository;
import wiki.chiu.micro.user.req.UserEntityRegisterReq;
import wiki.chiu.micro.user.req.UserEntityReq;
import wiki.chiu.micro.user.service.RegistrationService;
import wiki.chiu.micro.user.service.RegistrationTokenStore;
import wiki.chiu.micro.user.service.UserService;
import wiki.chiu.micro.user.support.PhonePlaceholderGenerator;

@Service
public class RegistrationServiceImpl implements RegistrationService {

  private final RegistrationTokenStore tokens;
  private final UserRepository users;
  private final UserService userService;

  @Value("${megalith.blog.register.page-prefix}")
  private String pagePrefix;

  public RegistrationServiceImpl(
      RegistrationTokenStore tokens, UserRepository users, UserService userService) {
    this.tokens = tokens;
    this.users = users;
    this.userService = userService;
  }

  @Override
  public String issuePage(String username) {
    String token = tokens.issue(username);
    return StringUtils.hasLength(username)
        ? pagePrefix + token + "?username=" + username
        : pagePrefix + token;
  }

  @Override
  public boolean isPageValid(String token) {
    return tokens.exists(token);
  }

  @Override
  @Transactional
  public void register(UserEntityRegisterReq request) {
    validatePolicy(request);
    UserEntityRegisterReq normalized =
        StringUtils.hasLength(request.phone())
            ? request
            : new UserEntityRegisterReq(request, PhonePlaceholderGenerator.generate());
    userService.saveOrUpdate(toUserRequest(normalized));
    tokens.consume(request.token());
  }

  private void validatePolicy(UserEntityRegisterReq request) {
    tokens.requireValid(request.token());
    users
        .findByUsername(request.username())
        .filter(user -> StatusEnum.HIDE.getCode().equals(user.getStatus()))
        .ifPresent(
            user -> {
              throw new ValidationException("registration arguments are invalid");
            });
    String registeredUsername = tokens.username(request.token());
    if (StringUtils.hasLength(registeredUsername)
        && !Objects.equals(registeredUsername, request.username())) {
      throw new MissException(NO_AUTH);
    }
  }

  private UserEntityReq toUserRequest(UserEntityRegisterReq request) {
    List<String> roles = List.of(USER);
    return users
        .findByUsername(request.username())
        .map(entity -> new UserEntityReq(request, entity.getId(), NORMAL.getCode(), roles))
        .orElseGet(() -> new UserEntityReq(request, null, NORMAL.getCode(), roles));
  }
}
