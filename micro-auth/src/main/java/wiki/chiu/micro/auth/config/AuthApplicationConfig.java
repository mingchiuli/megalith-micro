package wiki.chiu.micro.auth.config;

import java.time.Duration;

import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import wiki.chiu.micro.auth.adapter.in.http.SessionCookies;
import wiki.chiu.micro.auth.adapter.out.redis.RedisPasswordFailureStore;
import wiki.chiu.micro.auth.application.port.in.AuthService;
import wiki.chiu.micro.auth.application.port.in.CodeService;
import wiki.chiu.micro.auth.application.port.in.LoginSession;
import wiki.chiu.micro.auth.application.port.in.PasswordFailurePolicy;
import wiki.chiu.micro.auth.application.port.in.TokenService;
import wiki.chiu.micro.auth.application.port.out.AuthorizationDirectory;
import wiki.chiu.micro.auth.application.port.out.LoginCodeStore;
import wiki.chiu.micro.auth.application.port.out.MailSender;
import wiki.chiu.micro.auth.application.port.out.PasswordFailureStore;
import wiki.chiu.micro.auth.application.port.out.RouteTokenReader;
import wiki.chiu.micro.auth.application.port.out.SmsSender;
import wiki.chiu.micro.auth.application.port.out.TokenEncoder;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;
import wiki.chiu.micro.auth.application.port.out.VisitRecorder;
import wiki.chiu.micro.auth.application.service.AuthServiceImpl;
import wiki.chiu.micro.auth.application.service.CodeServiceImpl;
import wiki.chiu.micro.auth.application.service.LoginSessionServiceImpl;
import wiki.chiu.micro.auth.application.service.PasswordFailurePolicyServiceImpl;
import wiki.chiu.micro.auth.application.service.TokenServiceImpl;

/**
 * Wires the use cases to the adapters that implement their ports. The application layer carries no
 * Spring annotations, so every service bean is declared here.
 */
@Configuration(proxyBeanMethods = false)
public class AuthApplicationConfig {

    @Bean
    AuthService authService(
        AuthorizationDirectory authorizationDirectory, VisitRecorder visits, RouteTokenReader routeTokens) {
        return new AuthServiceImpl(authorizationDirectory, visits, routeTokens);
    }

    @Bean
    CodeService codeService(
        MailSender mails, LoginCodeStore codes, UserDirectory users, SmsSender smsSender) {
        return new CodeServiceImpl(mails, codes, users, smsSender);
    }

    @Bean
    TokenService tokenService(TokenEncoder tokens, UserDirectory users) {
        return new TokenServiceImpl(tokens, users);
    }

    @Bean
    LoginSession loginSession(
        PasswordFailureStore passwordFailures, UserDirectory users, TokenEncoder tokens) {
        return new LoginSessionServiceImpl(passwordFailures, users, tokens);
    }

    @Bean
    PasswordFailurePolicy passwordFailurePolicy(
        PasswordFailureStore failures, UserDirectory users, PasswordFailureProperties properties) {
        return new PasswordFailurePolicyServiceImpl(failures, users, properties.getMaxAttempts());
    }

    @Bean
    PasswordFailureStore passwordFailureStore(
        RedissonClient redissonClient,
        ResourceLoader resourceLoader,
        PasswordFailureProperties properties) {
        Duration window = properties.getWindow();
        return new RedisPasswordFailureStore(redissonClient, resourceLoader, window);
    }

    @Bean
    SessionCookies sessionCookies(JwtProperties jwt, TokenCookieProperties cookie) {
        return new SessionCookies(
            cookie.path(),
            cookie.secure(),
            cookie.sameSite(),
            jwt.accessTokenExpire(),
            jwt.refreshTokenExpire());
    }
}
