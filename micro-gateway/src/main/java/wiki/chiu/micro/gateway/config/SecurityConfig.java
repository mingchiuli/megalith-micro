package wiki.chiu.micro.gateway.config;


import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import wiki.chiu.micro.common.req.AuthorityRouteCheckReq;
import wiki.chiu.micro.gateway.rpc.AuthHttpServiceWrapper;

import java.util.Optional;


@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    public SecurityConfig(AuthHttpServiceWrapper authHttpServiceWrapper) {
        this.authHttpServiceWrapper = authHttpServiceWrapper;
    }

    @Bean
    SecurityFilterChain configure(HttpSecurity http) throws Exception {
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
                                .requestMatchers("/actuator/health")
                                .permitAll()
                                .anyRequest()
                                .access((_, context) -> {
                                    HttpServletRequest request = context.getRequest();
                                    String requestURI = request.getRequestURI();
                                    String method = request.getMethod();
                                    String token = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                                            .orElse("");
                                    AuthorityRouteCheckReq routeCheckReq = AuthorityRouteCheckReq.builder()
                                            .routeMapping(requestURI)
                                            .method(method)
                                            .build();
                                    Boolean b = authHttpServiceWrapper.routeCheck(routeCheckReq, token);
                                    return new AuthorizationDecision(b);
                                }))
                .build();
    }

}
