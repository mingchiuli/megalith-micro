package org.chiu.micro.auth.component.provider;

import org.chiu.micro.auth.lang.Const;
import org.chiu.micro.auth.lang.StatusEnum;
import org.chiu.micro.auth.rpc.wrapper.UserHttpServiceWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;

import static org.chiu.micro.auth.lang.ExceptionMessage.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author mingchiuli
 * @create 2023-01-14 9:02
 */
@Component
public final class PasswordAuthenticationProvider extends ProviderBase {

    private final PasswordEncoder passwordEncoder;

    private final StringRedisTemplate redisTemplate;

    private final ResourceLoader resourceLoader;

    @Value("${blog.password-error-intervalTime}")
    private long intervalTime;

    @Value("${blog.email-try-count}")
    private int maxTryNum;

    private String script;

    @PostConstruct
    @SneakyThrows
    private void init() {
        Resource resource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/password.lua");
        script = resource.getContentAsString(StandardCharsets.UTF_8);
    }

    public PasswordAuthenticationProvider(PasswordEncoder passwordEncoder,
                                          StringRedisTemplate redisTemplate,
                                          UserDetailsService userDetailsService,
                                          UserHttpServiceWrapper userHttpServiceWrapper,
                                          ResourceLoader resourceLoader) {
        super(userDetailsService, userHttpServiceWrapper);
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.equals(authentication);
    }

    @Override
    public void authProcess(UserDetails user, Authentication authentication) {

        Optional.ofNullable(authentication.getCredentials()).ifPresentOrElse(credentials -> {
            String presentedPassword = credentials.toString();
            if (!passwordEncoder.matches(presentedPassword, user.getPassword())) {
                String username = user.getUsername();
                passwordNotMatchProcess(username);
                throw new BadCredentialsException(PASSWORD_MISMATCH.getMsg());
            }
        }, () -> {
            throw new BadCredentialsException(PASSWORD_MISS.getMsg());
        });
    }

    private void passwordNotMatchProcess(String username) {
        String prefix = Const.PASSWORD_KEY.getInfo() + username;
        List<String> loginFailureTimeStampRecords = Optional.ofNullable(redisTemplate.opsForList().range(prefix, 0, -1)).orElseGet(ArrayList::new);
        int len = loginFailureTimeStampRecords.size();
        int l = 0;

        long currentTimeMillis = System.currentTimeMillis();

        for (String timestamp : loginFailureTimeStampRecords) {
            if (currentTimeMillis - Long.parseLong(timestamp) >= intervalTime) {
                l++;
            } else {
                break;
            }
        }

        if (len - l + 1 >= maxTryNum) {
            userHttpServiceWrapper.changeUserStatusByUsername(username, StatusEnum.HIDE.getCode());
        }

        redisTemplate.execute(RedisScript.of(script),
                Collections.singletonList(prefix),
                String.valueOf(l), "-1", String.valueOf(System.currentTimeMillis()), String.valueOf(intervalTime / 1000));
    }
}
