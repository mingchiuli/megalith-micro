package wiki.chiu.micro.auth.adapter.out.redis;

import static wiki.chiu.micro.common.constant.Const.CODE_KEY;
import static wiki.chiu.micro.common.constant.Const.EMAIL_KEY;
import static wiki.chiu.micro.common.constant.Const.PHONE_KEY;
import static wiki.chiu.micro.common.constant.Const.TRY_COUNT_KEY;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import wiki.chiu.micro.auth.application.port.out.LoginCodeStore;

@Component
public class RedisLoginCodeStore implements LoginCodeStore {

    private static final String CODE_TTL_SECONDS = "120";

    private final RedissonClient redisson;

    private final ResourceLoader resources;

    private String saveScript;

    private String verifyScript;

    private String cancelScript;

    public RedisLoginCodeStore(RedissonClient redisson, ResourceLoader resources) {
        this.redisson = redisson;
        this.resources = resources;
    }

    @PostConstruct
    void loadScripts() throws IOException {
        saveScript = readScript("reserve-login-code.lua");
        verifyScript = readScript("verify-login-code.lua");
        cancelScript = readScript("cancel-login-code.lua");
    }

    @Override
    public boolean reserve(LoginChannel channel, String principal, String code, String reservation) {
        return Boolean.TRUE.equals(redisson.getScript().eval(
            RScript.Mode.READ_WRITE, saveScript, RScript.ReturnType.BOOLEAN,
            Collections.singletonList(key(channel, principal)),
            CODE_KEY, code, TRY_COUNT_KEY, CODE_TTL_SECONDS, reservation));
    }

    @Override
    public void cancel(LoginChannel channel, String principal, String reservation) {
        redisson.getScript().eval(
            RScript.Mode.READ_WRITE, cancelScript, RScript.ReturnType.LONG,
            Collections.singletonList(key(channel, principal)), reservation);
    }

    @Override
    public Verification verify(
        LoginChannel channel, String principal, String presentedCode, int maxAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be positive");
        }
        String result = redisson.getScript().eval(
            RScript.Mode.READ_WRITE, verifyScript, RScript.ReturnType.VALUE,
            Collections.singletonList(key(channel, principal)),
            CODE_KEY, TRY_COUNT_KEY, presentedCode == null ? "" : presentedCode,
            Integer.toString(maxAttempts), channel == LoginChannel.EMAIL ? "1" : "0");
        return Verification.valueOf(result);
    }

    private static String key(LoginChannel channel, String principal) {
        return (channel == LoginChannel.EMAIL ? EMAIL_KEY : PHONE_KEY) + principal;
    }

    private String readScript(String name) throws IOException {
        return resources
            .getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/" + name)
            .getContentAsString(StandardCharsets.UTF_8);
    }
}
