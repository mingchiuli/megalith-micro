package wiki.chiu.micro.auth.config;

import wiki.chiu.micro.auth.component.manager.CustomProviderManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;

import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-11-27 5:56 pm
 */
@Configuration
public class AuthenticationManagerConfig {

    private final List<AuthenticationProvider> providers;

    public AuthenticationManagerConfig(List<AuthenticationProvider> providers) {
        this.providers = providers;
    }

    @Bean
    AuthenticationManager authenticationManager() {
        return new CustomProviderManager(providers);
    }
}
