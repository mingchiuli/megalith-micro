package org.chiu.micro.auth.service.impl;

import org.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import org.chiu.micro.auth.service.CodeService;
import org.chiu.micro.auth.utils.CodeFactory;
import org.chiu.micro.auth.utils.SmsUtils;
import org.chiu.micro.common.exception.CodeException;
import org.chiu.micro.common.lang.Const;
import org.chiu.micro.common.rpc.SmsHttpService;
import org.chiu.micro.common.utils.JsonUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

import static org.chiu.micro.common.lang.Const.SMS_CODE;
import static org.chiu.micro.common.lang.ExceptionMessage.CODE_EXISTED;


/**
 * @author mingchiuli
 * @create 2022-11-27 8:28 pm
 */
@Service
public class CodeServiceImpl implements CodeService {

    private final CodeFactory codeFactory;

    private final JavaMailSender javaMailSender;

    private final RedissonClient redissonClient;

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    private final SmsHttpService smsHttpService;

    private final SmsUtils smsUtils;

    private final JsonUtils jsonUtils;

    @Value("${spring.mail.properties.from}")
    private String from;

    public CodeServiceImpl( JsonUtils jsonUtils, CodeFactory codeFactory, JavaMailSender javaMailSender, RedissonClient redissonClient, UserHttpServiceWrapper userHttpServiceWrapper, SmsHttpService smsHttpService, SmsUtils smsUtils) {
        this.codeFactory = codeFactory;
        this.javaMailSender = javaMailSender;
        this.redissonClient = redissonClient;
        this.userHttpServiceWrapper = userHttpServiceWrapper;
        this.smsHttpService = smsHttpService;
        this.smsUtils = smsUtils;
        this.jsonUtils = jsonUtils;
    }


    @Override
    public void createEmailCode(String loginEmail) {
        userHttpServiceWrapper.findByEmail(loginEmail);
        String key = Const.EMAIL_KEY.getInfo() + loginEmail;
        boolean res = Boolean.FALSE.equals(redissonClient.getBucket(key).isExists());
        if (!res) {
            throw new CodeException(CODE_EXISTED);
        }

        Object code = codeFactory.create(Const.EMAIL_CODE.getInfo());
        var simpMsg = new SimpleMailMessage();
        simpMsg.setFrom(from);
        simpMsg.setTo(loginEmail);
        simpMsg.setSubject("login code");
        simpMsg.setText(code.toString());
        javaMailSender.send(simpMsg);
        codeFactory.save(code, key);
    }


    @Override
    public void createSMSCode(String loginSMS) {
        userHttpServiceWrapper.findByPhone(loginSMS);
        String key = Const.PHONE_KEY.getInfo() + loginSMS;
        boolean res = Boolean.FALSE.equals(redissonClient.getBucket(key).isExists());
        if (!res) {
            throw new CodeException(CODE_EXISTED);
        }

        Object code = codeFactory.create(SMS_CODE.getInfo());
        Map<String, Object> codeMap = Collections.singletonMap("code", code);
        String signature = smsUtils.getSignature(loginSMS, jsonUtils.writeValueAsString(codeMap));
        smsHttpService.sendSms("?Signature=" + signature);
        codeFactory.save(code, key);
    }
}
