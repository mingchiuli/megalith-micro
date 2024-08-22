package org.chiu.micro.auth.config;


import lombok.RequiredArgsConstructor;

import org.chiu.micro.auth.component.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final LoginFailureHandler loginFailureHandler;

    private final LoginSuccessHandler loginSuccessHandler;

    private final AuthenticationManager authenticationManager;

    @Bean
    SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(formLogin ->
                        formLogin
                                .successHandler(loginSuccessHandler)
                                .failureHandler(loginFailureHandler))
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests
                                .anyRequest()
                                .permitAll())
                .authenticationManager(authenticationManager)
                .build();
    }

}
