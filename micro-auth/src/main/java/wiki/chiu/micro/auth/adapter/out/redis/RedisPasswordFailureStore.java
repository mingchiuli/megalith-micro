package wiki.chiu.micro.auth.adapter.out.redis;

import static wiki.chiu.micro.common.constant.Const.PASSWORD_KEY;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ResourceLoader;

import org.springframework.util.ResourceUtils;

import wiki.chiu.micro.auth.application.port.out.PasswordFailureStore;

public class RedisPasswordFailureStore implements PasswordFailureStore {

    private final RedissonClient redisson;

    private final ResourceLoader resources;

    private final Duration window;

    private String script;

    public RedisPasswordFailureStore(
        RedissonClient redisson, ResourceLoader resources, Duration window) {
        this.redisson = redisson;
        this.resources = resources;
        this.window = window;
    }

    @PostConstruct
    void loadScript() throws IOException {
        script =
            resources
                .getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/password.lua")
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Override
    public long recordFailure(Long userId) {
        long now = System.currentTimeMillis();
        long windowMillis = window.toMillis();
        Number failures =
            redisson
                .getScript()
                .eval(
                    RScript.Mode.READ_WRITE,
                    script,
                    RScript.ReturnType.LONG,
                    Collections.singletonList(PASSWORD_KEY + userId),
                    String.valueOf(now - windowMillis),
                    String.valueOf(now),
                    now + ":" + UUID.randomUUID(),
                    String.valueOf(windowMillis));
        return failures.longValue();
    }

    @Override
    public void clear(Long userId) {
        redisson.getKeys().delete(PASSWORD_KEY + userId);
    }
}
