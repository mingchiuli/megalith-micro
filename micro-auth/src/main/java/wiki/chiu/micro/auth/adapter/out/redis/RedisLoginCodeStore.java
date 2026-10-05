package wiki.chiu.micro.auth.adapter.out.redis;

import static wiki.chiu.micro.common.constant.Const.CODE_KEY;
import static wiki.chiu.micro.common.constant.Const.EMAIL_KEY;
import static wiki.chiu.micro.common.constant.Const.PHONE_KEY;
import static wiki.chiu.micro.common.constant.Const.TRY_COUNT_KEY;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

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

    private String countAttemptScript;

    public RedisLoginCodeStore(RedissonClient redisson, ResourceLoader resources) {
        this.redisson = redisson;
        this.resources = resources;
    }

    @PostConstruct
    void loadScripts() throws IOException {
        saveScript = readScript("hmset-expire.lua");
        countAttemptScript = readScript("email-phone.lua");
    }

    @Override
    public boolean exists(LoginChannel channel, String principal) {
        return redisson.getBucket(key(channel, principal)).isExists();
    }

    @Override
    public void save(LoginChannel channel, String principal, String code) {
        redisson
            .getScript()
            .eval(
                RScript.Mode.READ_WRITE,
                saveScript,
                RScript.ReturnType.VALUE,
                Collections.singletonList(key(channel, principal)),
                CODE_KEY,
                code,
                TRY_COUNT_KEY,
                "0",
                CODE_TTL_SECONDS);
    }

    @Override
    public Verification verify(
        LoginChannel channel, String principal, String presentedCode, int maxAttempts) {
        String key = key(channel, principal);
        Map<String, String> entries = redisson.<String, String>getMap(key).readAllMap();

        if (entries.isEmpty()) {
            return Verification.NOT_FOUND;
        }
        String code = entries.get(CODE_KEY);
        if (Integer.parseInt(entries.get(TRY_COUNT_KEY)) >= maxAttempts) {
            redisson.getBucket(key).delete();
            return Verification.ATTEMPTS_EXCEEDED;
        }
        if (!matches(channel, code, presentedCode)) {
            Long ttl =
                redisson
                    .getScript()
                    .eval(
                        RScript.Mode.READ_WRITE,
                        countAttemptScript,
                        RScript.ReturnType.LONG,
                        Collections.singletonList(key),
                        TRY_COUNT_KEY);
            return Objects.equals(0L, ttl) ? Verification.EXPIRED : Verification.MISMATCH;
        }
        redisson.getKeys().delete(key);
        return Verification.ACCEPTED;
    }

    private static boolean matches(LoginChannel channel, String code, String presentedCode) {
        return channel == LoginChannel.EMAIL
            ? code.equalsIgnoreCase(presentedCode)
            : Objects.equals(code, presentedCode);
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
