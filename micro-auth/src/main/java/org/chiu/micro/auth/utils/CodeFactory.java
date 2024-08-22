package org.chiu.micro.auth.utils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.chiu.micro.auth.exception.CodeException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import jakarta.annotation.PostConstruct;

import static org.chiu.micro.auth.lang.Const.*;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Random;

/**
 * @author mingchiuli
 * @create 2023-03-05 1:04 am
 */
@Component
@RequiredArgsConstructor
public class CodeFactory {

    private final ResourceLoader resourceLoader;

    private String script;

    @PostConstruct
    @SneakyThrows
    private void init() {
        Resource resource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/save-code.lua");
        script = resource.getContentAsString(StandardCharsets.UTF_8);
    }

    private static final char[] code = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z',
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
    };

    private static final char[] sms = {
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
    };

    private final Random random = new Random();

    private final StringRedisTemplate redisTemplate;

    public String create(String type) {
        if (SMS_CODE.getInfo().equals(type)) {
            return createSMS();
        } else if (EMAIL_CODE.getInfo().equals(type)) {
            return createEmailCode();
        } else if (PHONE_CODE.getInfo().equals(type)) {
            return createPhone();
        }
        throw new CodeException("code type input error");
    }

    private String createEmailCode() {
        var builder = new StringBuilder();
        
        for (int i = 0; i < 5; i++) {
            int idx = random.nextInt(code.length);
            builder.append(code[idx]);
        }
        return builder.toString();
    }

    private String createSMS() {
        var builder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int idx = random.nextInt(sms.length);
            builder.append(sms[idx]);
        }
        return builder.toString();
    }

    private String createPhone() {
        var builder = new StringBuilder();
        builder.append(2);
        for (int i = 0; i < 10; i++) {
            int idx = random.nextInt(sms.length);
            builder.append(sms[idx]);
        }
        return builder.toString();
    }

    public void save(Object code, String prefix) {
        RedisScript<Void> lua = RedisScript.of(script);
        redisTemplate.execute(lua, Collections.singletonList(prefix),
                "code", code.toString(), "try_count", "0", "120");
    }
}
