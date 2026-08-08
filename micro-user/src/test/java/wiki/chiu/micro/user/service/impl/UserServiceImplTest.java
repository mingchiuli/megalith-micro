package wiki.chiu.micro.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.user.repository.UserRepository;
import wiki.chiu.micro.user.req.UserEntityRegisterReq;
import wiki.chiu.micro.user.service.RegistrationTokenStore;
import wiki.chiu.micro.user.service.UserService;

class UserServiceImplTest {

  @Test
  void registrationRejectsExpiredTokenBeforePersistence() {
    RegistrationTokenStore tokens = mock(RegistrationTokenStore.class);
    UserService userService = mock(UserService.class);
    RegistrationServiceImpl service =
        new RegistrationServiceImpl(tokens, mock(UserRepository.class), userService);
    UserEntityRegisterReq request =
        new UserEntityRegisterReq(
            "alice",
            "Alice",
            "https://example.com/avatar.jpg",
            "secret",
            "secret",
            "alice@example.com",
            "13800138000",
            "expired-token");

    doThrow(new MissException(wiki.chiu.micro.common.lang.ExceptionMessage.NO_AUTH))
        .when(tokens)
        .requireValid("expired-token");

    MissException exception = assertThrows(MissException.class, () -> service.register(request));

    assertEquals("没有权限", exception.getMessage());
    verify(userService, never()).saveOrUpdate(org.mockito.ArgumentMatchers.any());
  }
}
