package wiki.chiu.micro.auth.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

/**
 * @author mingchiuli
 * @create 2022-11-27 5:56 pm
 */
@Configuration(proxyBeanMethods = false)
public class AuthenticationManagerConfig {

  private final List<AuthenticationProvider> providers;

  public AuthenticationManagerConfig(List<AuthenticationProvider> providers) {
    this.providers = providers;
  }

  @Bean
  AuthenticationManager authenticationManager() {
    return new ProviderManager(providers);
  }
}
