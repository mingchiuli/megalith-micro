package org.chiu.micro.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.chiu.micro.gateway.component.JwtAuthenticationEntryPoint;
import org.chiu.micro.gateway.component.JwtAuthenticationFilter;
import org.chiu.micro.gateway.dto.AuthorityDto;
import org.chiu.micro.gateway.lang.Const;
import org.chiu.micro.gateway.rpc.wrapper.UserHttpServiceWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    @Bean
    SecurityFilterChain configure(HttpSecurity http) throws Exception {

        List<AuthorityDto> authorities = userHttpServiceWrapper.getAuthorities().stream()
                .filter(item -> Const.HTTP.getInfo().equals(item.getPrototype()))
                .toList();

        List<AuthorityDto> nonWhiteList = authorities.stream()
                .filter(item -> !item.getCode().startsWith(Const.WHITELIST.getInfo()))
                .toList();

        List<String> urlWhite = authorities.stream()
                .filter(item -> item.getCode().startsWith(Const.WHITELIST.getInfo()))
                .map(AuthorityDto::getRoutePattern)
                .toList();
  
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers(urlWhite.toArray(new String[0])).permitAll();
                    nonWhiteList.forEach(item -> authorize
                            .requestMatchers(HttpMethod.valueOf(item.getMethodType()), item.getRoutePattern())
                            .hasAuthority(item.getCode()));
                })
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
