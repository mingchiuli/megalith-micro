package wiki.chiu.micro.auth.component.provider;

import static wiki.chiu.micro.common.lang.ExceptionMessage.PASSWORD_MISMATCH;
import static wiki.chiu.micro.common.lang.ExceptionMessage.PASSWORD_MISS;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import wiki.chiu.micro.auth.adapter.out.http.UserHttpServiceWrapper;
import wiki.chiu.micro.auth.config.PasswordFailureProperties;
import wiki.chiu.micro.auth.user.LoginUser;
import wiki.chiu.micro.common.lang.Const;

/**
 * @author mingchiuli
 * @create 2023-01-14 9:02
 */
@Component
public final class PasswordAuthenticationProvider extends ProviderBase {

  private final PasswordEncoder passwordEncoder;

  private final RedissonClient redissonClient;

  private final ResourceLoader resourceLoader;
  private final PasswordFailureProperties properties;

  private String script;

  @PostConstruct
  private void init() throws IOException {
    Resource resource =
        resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/password.lua");
    script = resource.getContentAsString(StandardCharsets.UTF_8);
  }

  public PasswordAuthenticationProvider(
      PasswordEncoder passwordEncoder,
      RedissonClient redissonClient,
      UserDetailsService userDetailsService,
      UserHttpServiceWrapper userHttpServiceWrapper,
      ResourceLoader resourceLoader,
      PasswordFailureProperties properties) {
    super(userDetailsService, userHttpServiceWrapper);
    this.passwordEncoder = passwordEncoder;
    this.redissonClient = redissonClient;
    this.resourceLoader = resourceLoader;
    this.properties = properties;
  }

  @Override
  public boolean supports(@NonNull Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.equals(authentication);
  }

  @Override
  public void authProcess(UserDetails user, Authentication authentication) {
    Optional.ofNullable(authentication.getCredentials())
        .ifPresentOrElse(
            credentials -> {
              String presentedPassword = credentials.toString();
              if (!passwordEncoder.matches(presentedPassword, user.getPassword())) {
                handlePasswordMismatch(((LoginUser) user).getUserId());
              }
            },
            () -> {
              throw new BadCredentialsException(PASSWORD_MISS.getMsg());
            });
  }

  private void handlePasswordMismatch(Long userId) {
    long now = System.currentTimeMillis();
    long window = properties.getWindow().toMillis();
    Number failures =
        redissonClient
            .getScript()
            .eval(
                RScript.Mode.READ_WRITE,
                script,
                RScript.ReturnType.LONG,
                Collections.singletonList(Const.PASSWORD_KEY + userId),
                String.valueOf(now - window),
                String.valueOf(now),
                now + ":" + UUID.randomUUID(),
                String.valueOf(window));
    if (failures.longValue() >= properties.getMaxAttempts()) {
      userHttpServiceWrapper.lockAfterPasswordFailures(userId);
    }
    throw new BadCredentialsException(PASSWORD_MISMATCH.getMsg());
  }
}
