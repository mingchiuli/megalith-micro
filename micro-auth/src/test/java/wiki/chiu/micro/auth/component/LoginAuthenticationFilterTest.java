package wiki.chiu.micro.auth.component;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import tools.jackson.databind.json.JsonMapper;

class LoginAuthenticationFilterTest {

  private final LoginAuthenticationFilter filter =
      new LoginAuthenticationFilter(
          mock(AuthenticationManager.class),
          JsonMapper.builder().build(),
          mock(LoginSuccessHandler.class),
          mock(LoginFailureHandler.class));

  @Test
  void matchesPostLoginOnly() {
    assertTrue(filter.getRequestMatcher().matches(new MockHttpServletRequest("POST", "/login")));
    assertFalse(filter.getRequestMatcher().matches(new MockHttpServletRequest("GET", "/login")));
    assertFalse(filter.getRequestMatcher().matches(new MockHttpServletRequest("POST", "/other")));
  }
}
