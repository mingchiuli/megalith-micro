package org.chiu.micro.auth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.RequiredArgsConstructor;

import org.chiu.micro.auth.exception.CodeException;
import org.chiu.micro.auth.lang.Const;
import org.chiu.micro.auth.rpc.SmsHttpService;
import org.chiu.micro.auth.rpc.wrapper.UserHttpServiceWrapper;
import org.chiu.micro.auth.service.CodeService;
import org.chiu.micro.auth.utils.CodeFactory;
import org.chiu.micro.auth.utils.SmsUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import static org.chiu.micro.auth.lang.Const.*;
import static org.chiu.micro.auth.lang.ExceptionMessage.*;

import java.util.Collections;
import java.util.Map;


/**
 * @author mingchiuli
 * @create 2022-11-27 8:28 pm
 */
@Service
@RequiredArgsConstructor
public class CodeServiceImpl implements CodeService {

    private final CodeFactory codeFactory;

    private final JavaMailSender javaMailSender;

    private final StringRedisTemplate redisTemplate;

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    private final SmsHttpService smsHttpService;

    private final SmsUtils smsUtils;

    private final ObjectMapper objectMapper;

    @Value("${spring.mail.properties.from}")
    private String from;


    @Override
    public void createEmailCode(String loginEmail) {
        userHttpServiceWrapper.findByEmail(loginEmail);
        String key = Const.EMAIL_KEY.getInfo() + loginEmail;
        boolean res = Boolean.FALSE.equals(redisTemplate.hasKey(key));
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


    @SneakyThrows
    @Override
    public void createSMSCode(String loginSMS) {
        userHttpServiceWrapper.findByPhone(loginSMS);
        String key = Const.PHONE_KEY.getInfo() + loginSMS;
        boolean res = Boolean.FALSE.equals(redisTemplate.hasKey(key));
        if (!res) {
            throw new CodeException(CODE_EXISTED);
        }
        
        Object code = codeFactory.create(SMS_CODE.getInfo());
        Map<String, Object> codeMap = Collections.singletonMap("code", code);
        String signature = smsUtils.getSignature(loginSMS, objectMapper.writeValueAsString(codeMap));
        smsHttpService.sendSms("?Signature=" + signature);
        codeFactory.save(code, key);
    }
}
