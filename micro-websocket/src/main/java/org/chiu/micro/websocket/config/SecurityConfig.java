package org.chiu.micro.websocket.config;


import org.chiu.micro.websocket.dto.AuthorityDto;
import org.chiu.micro.websocket.lang.Const;
import org.chiu.micro.websocket.rpc.wrapper.AuthHttpServiceWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;


@Configuration
public class SecurityConfig {

    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    public SecurityConfig(AuthHttpServiceWrapper authHttpServiceWrapper) {
        this.authHttpServiceWrapper = authHttpServiceWrapper;
    }

    @Bean
    SecurityFilterChain configure(HttpSecurity http) throws Exception {

        List<String> whitelist = authHttpServiceWrapper.getSystemAuthorities().stream()
                .filter(item -> Const.HTTP.getInfo().equals(item.getPrototype()))
                .filter(item -> item.getCode().startsWith(Const.WHITELIST.getInfo()))
                .map(AuthorityDto::getRoutePattern)
                .toList();

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
                                .requestMatchers(whitelist.toArray(new String[0]))
                                .permitAll()
                                .anyRequest()
                                .authenticated())
                .build();
    }

}
