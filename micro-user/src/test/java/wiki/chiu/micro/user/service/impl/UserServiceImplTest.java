package wiki.chiu.micro.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;
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
        .consumeForUsername("expired-token", "alice");

    MissException exception = assertThrows(MissException.class, () -> service.register(request));

    assertEquals("没有权限", exception.getMessage());
    verify(userService, never()).saveOrUpdate(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void registrationConsumesTokenBeforePersistingUser() {
    RegistrationTokenStore tokens = mock(RegistrationTokenStore.class);
    UserRepository users = mock(UserRepository.class);
    UserService userService = mock(UserService.class);
    RegistrationServiceImpl service = new RegistrationServiceImpl(tokens, users, userService);
    when(users.findByUsername("alice")).thenReturn(Optional.empty());
    UserEntityRegisterReq request =
        new UserEntityRegisterReq(
            "alice",
            "Alice",
            "https://example.com/avatar.jpg",
            "secret",
            "secret",
            "alice@example.com",
            "13800138000",
            "token");

    service.register(request);

    InOrder order = inOrder(tokens, userService);
    order.verify(tokens).consumeForUsername("token", "alice");
    order.verify(userService).saveOrUpdate(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void registrationPageEncodesUsernameAsOneQueryParameter() {
    RegistrationTokenStore tokens = mock(RegistrationTokenStore.class);
    when(tokens.issue("张 三&#")).thenReturn("token");
    RegistrationServiceImpl service =
        new RegistrationServiceImpl(tokens, mock(UserRepository.class), mock(UserService.class));
    ReflectionTestUtils.setField(service, "pagePrefix", "https://example.com/register/");

    String page = service.issuePage("张 三&#");

    String rawQuery = URI.create(page).getRawQuery();
    assertFalse(rawQuery.substring("username=".length()).contains("&"));
    assertFalse(rawQuery.contains("#"));
    assertEquals("username=张 三&#", URLDecoder.decode(rawQuery, StandardCharsets.UTF_8));
  }
}
