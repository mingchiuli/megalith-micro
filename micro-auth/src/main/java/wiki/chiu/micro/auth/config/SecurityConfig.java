package wiki.chiu.micro.auth.config;


import wiki.chiu.micro.auth.component.LoginAuthenticationConverter;
import wiki.chiu.micro.auth.component.LoginAuthenticationFilter;
import wiki.chiu.micro.auth.component.LoginFailureHandler;
import wiki.chiu.micro.auth.component.LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    private final LoginFailureHandler loginFailureHandler;

    private final LoginSuccessHandler loginSuccessHandler;

    private final AuthenticationManager authenticationManager;

    private final LoginAuthenticationConverter loginAuthenticationConverter;

    public SecurityConfig(LoginFailureHandler loginFailureHandler,
                          LoginSuccessHandler loginSuccessHandler,
                          AuthenticationManager authenticationManager,
                          LoginAuthenticationConverter loginAuthenticationConverter) {
        this.loginFailureHandler = loginFailureHandler;
        this.loginSuccessHandler = loginSuccessHandler;
        this.authenticationManager = authenticationManager;
        this.loginAuthenticationConverter = loginAuthenticationConverter;
    }

    @Bean
    SecurityFilterChain configure(HttpSecurity http) throws Exception {
        LoginAuthenticationFilter loginAuthenticationFilter = new LoginAuthenticationFilter(
                authenticationManager,
                loginAuthenticationConverter,
                loginSuccessHandler,
                loginFailureHandler);

        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests
                                .anyRequest()
                                .permitAll())
                .addFilterAt(loginAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
