package wiki.chiu.micro.auth.component.provider;

import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.common.lang.Const;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import jakarta.annotation.PostConstruct;
import wiki.chiu.micro.common.lang.StatusEnum;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static wiki.chiu.micro.common.lang.ExceptionMessage.PASSWORD_MISMATCH;
import static wiki.chiu.micro.common.lang.ExceptionMessage.PASSWORD_MISS;

/**
 * @author mingchiuli
 * @create 2023-01-14 9:02
 */
@Component
public final class PasswordAuthenticationProvider extends ProviderBase {

    private final PasswordEncoder passwordEncoder;

    private final RedissonClient redissonClient;

    private final ResourceLoader resourceLoader;

    @Value("${megalith.blog.password-error-intervalTime}")
    private long intervalTime;

    @Value("${megalith.blog.email-try-count}")
    private int maxTryNum;

    private String script;

    @PostConstruct
    private void init() throws IOException {
        Resource resource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/password.lua");
        script = resource.getContentAsString(StandardCharsets.UTF_8);
    }

    public PasswordAuthenticationProvider(PasswordEncoder passwordEncoder,
                                          RedissonClient redissonClient,
                                          UserDetailsService userDetailsService,
                                          UserHttpServiceWrapper userHttpServiceWrapper,
                                          ResourceLoader resourceLoader) {
        super(userDetailsService, userHttpServiceWrapper);
        this.passwordEncoder = passwordEncoder;
        this.redissonClient = redissonClient;
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
        String prefix = Const.PASSWORD_KEY + username;
        List<String> loginFailureTimeStampRecords = redissonClient.<String>getList(prefix).range(0, -1);
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

        redissonClient.getScript().eval(RScript.Mode.READ_WRITE, script, RScript.ReturnType.VALUE, Collections.singletonList(prefix), String.valueOf(l), "-1", String.valueOf(System.currentTimeMillis()), String.valueOf(intervalTime / 1000));
    }
}
