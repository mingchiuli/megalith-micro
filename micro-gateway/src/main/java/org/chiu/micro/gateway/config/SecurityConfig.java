package org.chiu.micro.gateway.config;

import lombok.RequiredArgsConstructor;

import org.chiu.micro.gateway.component.JwtAuthenticationEntryPoint;
import org.chiu.micro.gateway.component.JwtAuthenticationFilter;
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
public class SecurityConfig {


    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
        
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] URL_WHITELIST = {
            "/code/**",
            "/public/blog/**",
            "/search/public/**",
            "/actuator/**",
            "/sys/user/register/**",
            "/inner/auth/*",
            "/login"
    };

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
                                .requestMatchers(URL_WHITELIST)
                                .permitAll()
                                .requestMatchers(HttpMethod.GET, "/auth/menu/nav")
                                .hasAuthority("auth:menu:nav")
                                .requestMatchers(HttpMethod.GET, "/token/refresh")
                                .hasAuthority("token:refresh")
                                .requestMatchers(HttpMethod.GET, "/token/userinfo")
                                .hasAuthority("token:userinfo")
                                .requestMatchers(HttpMethod.POST, "/sys/blog/save")
                                .hasAuthority("sys:blog:save")
                                .requestMatchers(HttpMethod.POST, "/sys/blog/delete")
                                .hasAuthority("sys:blog:delete")
                                .requestMatchers(HttpMethod.GET, "/sys/blog/lock/*")
                                .hasAuthority("sys:blog:lock")
                                .requestMatchers(HttpMethod.GET, "/sys/blog/blogs")
                                .hasAuthority("sys:blog:blogs")
                                .requestMatchers(HttpMethod.GET, "/sys/blog/deleted")
                                .hasAuthority("sys:blog:deleted")
                                .requestMatchers(HttpMethod.GET, "/sys/blog/recover/*")
                                .hasAuthority("sys:blog:recover")
                                .requestMatchers(HttpMethod.POST, "/sys/blog/oss/upload")
                                .hasAuthority("sys:blog:oss:upload")
                                .requestMatchers(HttpMethod.GET, "/sys/blog/oss/delete")
                                .hasAuthority("sys:blog:oss:delete")
                                .requestMatchers(HttpMethod.GET, "/sys/blog/download")
                                .hasAuthority("sys:blog:download")
                                .requestMatchers(HttpMethod.POST, "/sys/blog/edit/push/all")
                                .hasAuthority("sys:blog:push:all")
                                .requestMatchers(HttpMethod.GET, "/sys/blog/edit/pull/echo")
                                .hasAuthority("sys:blog:echo")
                                .requestMatchers(HttpMethod.GET, "/search/sys/blogs")
                                .hasAuthority("sys:search:blogs")
                                .requestMatchers(HttpMethod.GET, "/sys/authority/list")
                                .hasAuthority("sys:authority:list")
                                .requestMatchers(HttpMethod.GET, "/sys/authority/info/*")
                                .hasAuthority("sys:authority:info")
                                .requestMatchers(HttpMethod.POST, "/sys/authority/save")
                                .hasAuthority("sys:authority:save")
                                .requestMatchers(HttpMethod.POST, "/sys/authority/delete")
                                .hasAuthority("sys:authority:delete")
                                .requestMatchers(HttpMethod.GET, "/sys/authority/download")
                                .hasAuthority("sys:authority:download")
                                .requestMatchers(HttpMethod.GET, "/sys/menu/info/*")
                                .hasAuthority("sys:menu:info")
                                .requestMatchers(HttpMethod.GET, "/sys/menu/list")
                                .hasAuthority("sys:menu:list")
                                .requestMatchers(HttpMethod.POST, "/sys/menu/save")
                                .hasAuthority("sys:menu:save")
                                .requestMatchers(HttpMethod.POST, "/sys/menu/delete/*")
                                .hasAuthority("sys:menu:delete")
                                .requestMatchers(HttpMethod.GET, "/sys/menu/download")
                                .hasAuthority("sys:menu:download")
                                .requestMatchers(HttpMethod.GET, "/sys/role/info/*")
                                .hasAuthority("sys:role:info")
                                .requestMatchers(HttpMethod.GET, "/sys/role/roles")
                                .hasAuthority("sys:role:roles")
                                .requestMatchers(HttpMethod.POST, "/sys/role/save")
                                .hasAuthority("sys:role:save")
                                .requestMatchers(HttpMethod.POST, "/sys/role/delete")
                                .hasAuthority("sys:role:delete")
                                .requestMatchers(HttpMethod.POST, "/sys/role/menu/*")
                                .hasAuthority("sys:role:menu:save")
                                .requestMatchers(HttpMethod.GET, "/sys/role/menu/*")
                                .hasAuthority("sys:role:menu:get")
                                .requestMatchers(HttpMethod.POST, "/sys/role/authority/*")
                                .hasAuthority("sys:role:authority:save")
                                .requestMatchers(HttpMethod.GET, "/sys/role/authority/*")
                                .hasAuthority("sys:role:authority:get")
                                .requestMatchers(HttpMethod.GET, "/sys/role/download")
                                .hasAuthority("sys:role:download")
                                .requestMatchers(HttpMethod.GET, "/sys/role/valid/all")
                                .hasAuthority("sys:role:valid:all")
                                .requestMatchers(HttpMethod.GET, "/sys/user/auth/register/page")
                                .hasAuthority("sys:user:register:page")
                                .requestMatchers(HttpMethod.POST, "/sys/user/save")
                                .hasAuthority("sys:user:save")
                                .requestMatchers(HttpMethod.GET, "/sys/user/page/*")
                                .hasAuthority("sys:user:page")
                                .requestMatchers(HttpMethod.POST, "/sys/user/delete")
                                .hasAuthority("sys:user:delete")
                                .requestMatchers(HttpMethod.GET, "/sys/user/info/*")
                                .hasAuthority("sys:user:info")
                                .requestMatchers(HttpMethod.GET, "/sys/user/download")
                                .hasAuthority("sys:user:download")
                                .anyRequest()
                                .authenticated())
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
