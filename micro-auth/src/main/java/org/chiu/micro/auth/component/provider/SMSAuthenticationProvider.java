package org.chiu.micro.auth.component.provider;

import org.chiu.micro.auth.component.token.SMSAuthenticationToken;
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
 * @create 2023-03-08 1:59 am
 */
@Component
public final class SMSAuthenticationProvider extends ProviderBase {

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

    public SMSAuthenticationProvider(UserDetailsService userDetailsService,
                                     RedissonClient redissonClient,
                                     UserHttpServiceWrapper userHttpServiceWrapper,
                                     ResourceLoader resourceLoader) {
        super(userDetailsService, userHttpServiceWrapper);
        this.redissonClient = redissonClient;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SMSAuthenticationToken.class.equals(authentication);
    }

    @Override
    protected void authProcess(UserDetails user, Authentication authentication) {
        String prefix = Const.PHONE_KEY.getInfo() + user.getUsername();
        Map<String, String> entries = redissonClient.<String, String>getMap(prefix).readAllMap();

        if (!entries.isEmpty()) {
            String code = entries.get("code");
            String tryCount = entries.get("try_count");

            if (Integer.parseInt(tryCount) >= maxTryNum) {
                redissonClient.getBucket(prefix).delete();
                throw new BadCredentialsException(SMS_TRY_MAX.getMsg());
            }

            if (!Objects.equals(code, authentication.getCredentials().toString())) {
                Long ttl = redissonClient.getScript().eval(RScript.Mode.READ_WRITE, script, RScript.ReturnType.INTEGER, Collections.singletonList(prefix), "try_count");
                if (Long.valueOf(0).equals(ttl)) {
                    throw new BadCredentialsException(SMS_EXPIRED.getMsg());
                }
                throw new BadCredentialsException(SMS_MISMATCH.getMsg());
            }

            redissonClient.getBucket(prefix).delete();
            return;
        }

        throw new BadCredentialsException(SMS_NOT_EXIST.getMsg());
    }
}
