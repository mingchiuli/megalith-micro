package wiki.chiu.micro.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wiki.chiu.micro.auth.service.port.UserDirectory;
import wiki.chiu.micro.auth.token.JwtTokenService;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

  @Mock private JwtTokenService jwtTokenService;

  @Mock private UserDirectory users;

  @InjectMocks private TokenServiceImpl tokenService;

  @Test
  void refreshAccessTokenIssuesRawTokenForActiveUser() {
    when(users.findById(42L)).thenReturn(user(0));
    when(jwtTokenService.issueAccessToken(42L)).thenReturn("jwt");

    String result = tokenService.refreshAccessToken(42L);

    assertEquals("jwt", result);
    verify(jwtTokenService).issueAccessToken(42L);
  }

  @Test
  void refreshAccessTokenRejectsDisabledUser() {
    when(users.findById(42L)).thenReturn(user(1));

    MissException exception =
        assertThrows(MissException.class, () -> tokenService.refreshAccessToken(42L));

    assertEquals("没有权限", exception.getMessage());
  }

  private UserEntityRpcVo user(int status) {
    return UserEntityRpcVo.builder().id(42L).status(status).build();
  }
}
