package org.chiu.micro.auth.component.provider;

import org.chiu.micro.auth.component.token.SMSAuthenticationToken;
import org.chiu.micro.auth.lang.Const;
import org.chiu.micro.auth.rpc.wrapper.UserHttpServiceWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;

import static org.chiu.micro.auth.lang.ExceptionMessage.*;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author mingchiuli
 * @create 2023-03-08 1:59 am
 */
@Component
public final class SMSAuthenticationProvider extends ProviderBase {

    private final StringRedisTemplate redisTemplate;

    @Value("${blog.email-try-count}")
    private int maxTryNum;


    private final ResourceLoader resourceLoader;

    private String script;

    @PostConstruct
    @SneakyThrows
    private void init() {
        Resource resource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/email-phone.lua");
        script = resource.getContentAsString(StandardCharsets.UTF_8);
    }

    public SMSAuthenticationProvider(UserDetailsService userDetailsService,
                                     StringRedisTemplate redisTemplate,
                                     UserHttpServiceWrapper userHttpServiceWrapper,
                                     ResourceLoader resourceLoader) {
        super(userDetailsService, userHttpServiceWrapper);
        this.redisTemplate = redisTemplate;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SMSAuthenticationToken.class.equals(authentication);
    }

    @Override
    protected void authProcess(UserDetails user, Authentication authentication) {
        String prefix = Const.PHONE_KEY.getInfo() + user.getUsername();
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        Map<String, String> entries = hashOperations.entries(prefix);

        if (!entries.isEmpty()) {
            String code = entries.get("code");
            String tryCount = entries.get("try_count");

            if (Integer.parseInt(tryCount) >= maxTryNum) {
                redisTemplate.delete(prefix);
                throw new BadCredentialsException(SMS_TRY_MAX.getMsg());
            }

            if (!Objects.equals(code, authentication.getCredentials().toString())) {
                Long ttl = redisTemplate.execute(RedisScript.of(script, Long.class),
                        Collections.singletonList(prefix), "try_count");
                if (Long.valueOf(0).equals(ttl)) {
                    throw new BadCredentialsException(SMS_EXPIRED.getMsg());
                }
                throw new BadCredentialsException(SMS_MISMATCH.getMsg());
            }

            redisTemplate.delete(prefix);
            return;
        }

        throw new BadCredentialsException(SMS_NOT_EXIST.getMsg());
    }
}
