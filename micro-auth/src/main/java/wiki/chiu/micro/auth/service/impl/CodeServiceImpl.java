package wiki.chiu.micro.auth.service.impl;

import jakarta.annotation.PostConstruct;
import org.redisson.api.RScript;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;
import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.auth.service.CodeService;
import wiki.chiu.micro.common.utils.CodeUtils;
import wiki.chiu.micro.auth.utils.SmsUtils;
import wiki.chiu.micro.common.exception.CodeException;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.rpc.SmsHttpService;
import wiki.chiu.micro.common.utils.JsonUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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

    private final SmsUtils smsUtils;

    @Value("${spring.mail.properties.from}")
    private String from;

    private String script;

    public CodeServiceImpl(JavaMailSender javaMailSender, RedissonClient redissonClient, UserHttpServiceWrapper userHttpServiceWrapper, SmsHttpService smsHttpService, ResourceLoader resourceLoader, SmsUtils smsUtils) {
        this.javaMailSender = javaMailSender;
        this.redissonClient = redissonClient;
        this.userHttpServiceWrapper = userHttpServiceWrapper;
        this.smsHttpService = smsHttpService;
        this.resourceLoader = resourceLoader;
        this.smsUtils = smsUtils;
    }

    @PostConstruct
    private void init() throws IOException {
        Resource resource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/save-code.lua");
        script = resource.getContentAsString(StandardCharsets.UTF_8);
    }


    @Override
    public void createEmailCode(String loginEmail) {
        userHttpServiceWrapper.findByEmail(loginEmail);
        String key = Const.EMAIL_KEY + loginEmail;
        boolean res = Boolean.FALSE.equals(redissonClient.getBucket(key).isExists());
        if (!res) {
            throw new CodeException(CODE_EXISTED);
        }

        Object code = CodeUtils.create(Const.EMAIL_CODE);
        var simpMsg = new SimpleMailMessage();
        simpMsg.setFrom(from);
        simpMsg.setTo(loginEmail);
        simpMsg.setSubject("login code");
        simpMsg.setText(code.toString());
        javaMailSender.send(simpMsg);
        redissonClient.getScript().eval(RScript.Mode.READ_WRITE, script, RScript.ReturnType.VALUE, Collections.singletonList(key), "code", code.toString(), "try_count", "0", "120");
    }


    @Override
    public void createSMSCode(String loginSMS) {
        userHttpServiceWrapper.findByPhone(loginSMS);
        String key = Const.PHONE_KEY + loginSMS;
        boolean res = Boolean.FALSE.equals(redissonClient.getBucket(key).isExists());
        if (!res) {
            throw new CodeException(CODE_EXISTED);
        }

        Object code = CodeUtils.create(SMS_CODE);
        Map<String, Object> codeMap = Collections.singletonMap("code", code);
        String signature = smsUtils.getSignature(loginSMS, JsonUtils.writeValueAsString(codeMap));
        smsHttpService.sendSms("?Signature=" + signature);
        redissonClient.getScript().eval(RScript.Mode.READ_WRITE, script, RScript.ReturnType.VALUE, Collections.singletonList(key), "code", code.toString(), "try_count", "0", "120");
    }
}
