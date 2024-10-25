package org.chiu.micro.auth.component.provider;

import org.chiu.micro.auth.component.token.EmailAuthenticationToken;
import org.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import org.chiu.micro.common.lang.Const;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static org.chiu.micro.common.lang.ExceptionMessage.*;

/**
 * @author mingchiuli
 * @create 2022-12-30 10:57 am
 */

@Component
public final class EmailAuthenticationProvider extends ProviderBase {

    private final RedissonClient redissonClient;

    @Value("${megalith.blog.email-try-count}")
    private int maxTryNum;

    private final ResourceLoader resourceLoader;

    private String script;

    @PostConstruct
    private void init() throws IOException {
        Resource resource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/email-phone.lua");
        script = resource.getContentAsString(StandardCharsets.UTF_8);
    }

    public EmailAuthenticationProvider(RedissonClient redissonClient,
                                       UserDetailsService userDetailsService,
                                       UserHttpServiceWrapper userHttpServiceWrapper,
                                       ResourceLoader resourceLoader) {
        super(userDetailsService, userHttpServiceWrapper);
        this.redissonClient = redissonClient;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return EmailAuthenticationToken.class.equals(authentication);
    }

    @Override
    public void authProcess(UserDetails user, Authentication authentication) {
        //username is login email
        String prefix = Const.EMAIL_KEY + user.getUsername();
        Map<String, String> entries = redissonClient.<String, String>getMap(prefix).readAllMap();


        if (!entries.isEmpty()) {
            String code = entries.get("code");
            String tryCount = entries.get("try_count");

            if (Integer.parseInt(tryCount) >= maxTryNum) {
                redissonClient.getBucket(prefix).delete();
                throw new BadCredentialsException(CODE_TRY_MAX.getMsg());
            }

            if (Boolean.FALSE.equals(code.equalsIgnoreCase(authentication.getCredentials().toString()))) {
                Long ttl = redissonClient.getScript().eval(RScript.Mode.READ_WRITE, script, RScript.ReturnType.INTEGER, Collections.singletonList(prefix), "try_count");

                if (Objects.equals(0L, ttl)) {
                    throw new BadCredentialsException(CODE_EXPIRED.getMsg());
                }
                throw new BadCredentialsException(CODE_MISMATCH.getMsg());
            }

            redissonClient.getKeys().delete(prefix);
            return;
        }

        throw new BadCredentialsException(CODE_NOT_EXIST.getMsg());

    }
}
