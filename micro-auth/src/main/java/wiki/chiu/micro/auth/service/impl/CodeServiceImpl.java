package wiki.chiu.micro.auth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.redisson.api.RScript;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;
import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.auth.service.CodeService;
import wiki.chiu.micro.common.utils.CodeUtils;
import wiki.chiu.micro.common.exception.CodeException;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.rpc.SmsHttpService;
import wiki.chiu.micro.common.utils.JsonUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.common.utils.SmsUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static wiki.chiu.micro.common.lang.Const.SMS_CODE;
import static wiki.chiu.micro.common.lang.ExceptionMessage.CODE_EXISTED;


/**
 * @author mingchiuli
 * @create 2022-11-27 8:28 pm
 */
@Service
public class CodeServiceImpl implements CodeService {

    private final JavaMailSender javaMailSender;

    private final RedissonClient redissonClient;

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    private final SmsHttpService smsHttpService;

    private final ResourceLoader resourceLoader;

    private final ObjectMapper objectMapper;

    @Value("${spring.mail.properties.from}")
    private String from;

    @Value("${megalith.blog.aliyun.access-key-id}")
    private String accessKeyId;

    @Value("${megalith.blog.aliyun.access-key-secret}")
    private String accessKeySecret;

    private String script;

    public CodeServiceImpl(JavaMailSender javaMailSender, RedissonClient redissonClient, UserHttpServiceWrapper userHttpServiceWrapper, SmsHttpService smsHttpService, ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.javaMailSender = javaMailSender;
        this.redissonClient = redissonClient;
        this.userHttpServiceWrapper = userHttpServiceWrapper;
        this.smsHttpService = smsHttpService;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    private void init() throws IOException {
        Resource resource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/save-code.lua");
        script = resource.getContentAsString(StandardCharsets.UTF_8);
    }


    @Override
    public void createEmailCode(String loginEmail) {
        validateUserEmail(loginEmail);
        String key = Const.EMAIL_KEY + loginEmail;
        checkCodeExistence(key);

        Object code = CodeUtils.create(Const.EMAIL_CODE);
        sendEmail(loginEmail, code.toString());
        saveCodeToRedis(key, code.toString());
    }

    @Override
    public void createSMSCode(String loginSMS) {
        validateUserPhone(loginSMS);
        String key = Const.PHONE_KEY + loginSMS;
        checkCodeExistence(key);

        Object code = CodeUtils.create(SMS_CODE);
        sendSms(loginSMS, code);
        saveCodeToRedis(key, code.toString());
    }

    private void validateUserEmail(String email) {
        userHttpServiceWrapper.findByEmail(email);
    }

    private void validateUserPhone(String phone) {
        userHttpServiceWrapper.findByPhone(phone);
    }

    private void checkCodeExistence(String key) {
        if (Boolean.TRUE.equals(redissonClient.getBucket(key).isExists())) {
            throw new CodeException(CODE_EXISTED);
        }
    }

    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Login Code");
        message.setText(code);
        javaMailSender.send(message);
    }

    private void sendSms(String phone, Object code) {
        Map<String, Object> codeMap = Collections.singletonMap("code", code);
        String signature = SmsUtils.getSignature(phone, JsonUtils.writeValueAsString(objectMapper, codeMap), accessKeyId, accessKeySecret);
        smsHttpService.sendSms("?Signature=" + signature);
    }

    private void saveCodeToRedis(String key, String code) {
        redissonClient.getScript().eval(RScript.Mode.READ_WRITE, script, RScript.ReturnType.VALUE, Collections.singletonList(key), "code", code, "try_count", "0", "120");
    }
}
