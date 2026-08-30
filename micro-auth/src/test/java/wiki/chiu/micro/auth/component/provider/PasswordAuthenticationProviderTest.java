package wiki.chiu.micro.auth.component.provider;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import wiki.chiu.micro.auth.adapter.out.http.UserHttpServiceWrapper;
import wiki.chiu.micro.auth.config.PasswordFailureProperties;
import wiki.chiu.micro.auth.user.LoginUser;
import wiki.chiu.micro.common.lang.Const;

class PasswordAuthenticationProviderTest {

  @Test
  void thirdFailureWithinTheWindowLocksTheAccount() {
    TestContext context = context(3L);

    assertThrows(BadCredentialsException.class, context::authenticate);

    verify(context.users).lockAfterPasswordFailures(42L);
  }

  @Test
  void failureBelowTheThresholdDoesNotLockTheAccount() {
    TestContext context = context(2L);

    assertThrows(BadCredentialsException.class, context::authenticate);

    verify(context.users, never()).lockAfterPasswordFailures(42L);
  }

  private TestContext context(long failureCount) {
    PasswordEncoder encoder = org.mockito.Mockito.mock(PasswordEncoder.class);
    RedissonClient redisson = org.mockito.Mockito.mock(RedissonClient.class);
    RScript redisScript = org.mockito.Mockito.mock(RScript.class);
    UserHttpServiceWrapper users = org.mockito.Mockito.mock(UserHttpServiceWrapper.class);
    when(encoder.matches("wrong", "encoded")).thenReturn(false);
    when(redisson.getScript()).thenReturn(redisScript);
    when(redisScript.eval(
            eq(RScript.Mode.READ_WRITE),
            anyString(),
            eq(RScript.ReturnType.LONG),
            eq(List.of(Const.PASSWORD_KEY + 42L)),
            any(),
            any(),
            any(),
            any()))
        .thenReturn(failureCount);
    PasswordFailureProperties properties = new PasswordFailureProperties();
    PasswordAuthenticationProvider provider =
        new PasswordAuthenticationProvider(
            encoder,
            redisson,
            org.mockito.Mockito.mock(UserDetailsService.class),
            users,
            new DefaultResourceLoader(),
            properties);
    ReflectionTestUtils.invokeMethod(provider, "init");
    LoginUser user = new LoginUser("alice", "encoded", true, true, true, true, List.of(), 42L);
    return new TestContext(provider, users, user);
  }

  private record TestContext(
      PasswordAuthenticationProvider provider, UserHttpServiceWrapper users, LoginUser user) {

    void authenticate() {
      provider.authProcess(user, new UsernamePasswordAuthenticationToken("alice", "wrong"));
    }
  }
}
