package wiki.chiu.micro.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.auth.component.LoginAuthenticationFilter;
import wiki.chiu.micro.auth.component.LoginFailureHandler;
import wiki.chiu.micro.auth.component.LoginSuccessHandler;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    private final LoginFailureHandler loginFailureHandler;

    private final LoginSuccessHandler loginSuccessHandler;

    private final AuthenticationManager authenticationManager;

    public SecurityConfig(LoginFailureHandler loginFailureHandler,
                          LoginSuccessHandler loginSuccessHandler,
                          AuthenticationManager authenticationManager) {
        this.loginFailureHandler = loginFailureHandler;
        this.loginSuccessHandler = loginSuccessHandler;
        this.authenticationManager = authenticationManager;
    }

    @Bean
    @Order(1)
    SecurityFilterChain refreshTokenChain(
            HttpSecurity http,
            @Qualifier("refreshJwtDecoder") JwtDecoder refreshJwtDecoder) throws Exception {
        return stateless(http)
                .securityMatcher("/token/refresh")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationConverter(new BearerTokenAuthenticationConverter())
                        .jwt(jwt -> jwt.decoder(refreshJwtDecoder)))
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain internalChain(HttpSecurity http) throws Exception {
        return stateless(http)
                .securityMatcher("/inner/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .build();
    }

    @Bean
    @Order(3)
    SecurityFilterChain applicationChain(
            HttpSecurity http,
            @Qualifier("accessJwtDecoder") JwtDecoder accessJwtDecoder,
            JsonMapper jsonMapper) throws Exception {
        LoginAuthenticationFilter loginAuthenticationFilter = new LoginAuthenticationFilter(
                authenticationManager,
                jsonMapper,
                loginSuccessHandler,
                loginFailureHandler);

        return stateless(http)
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests
                                .requestMatchers("/token/userinfo", "/auth/menu/nav")
                                .authenticated()
                                .anyRequest()
                                .permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationConverter(new BearerTokenAuthenticationConverter())
                        .jwt(jwt -> jwt.decoder(accessJwtDecoder)))
                .addFilterAt(loginAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private HttpSecurity stateless(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }
}
