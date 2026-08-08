package wiki.chiu.micro.auth.service.impl;

import static wiki.chiu.micro.common.lang.Const.SMS_CODE;
import static wiki.chiu.micro.common.lang.ExceptionMessage.CODE_EXISTED;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.auth.service.CodeService;
import wiki.chiu.micro.auth.service.port.SmsSender;
import wiki.chiu.micro.auth.service.port.UserDirectory;
import wiki.chiu.micro.auth.support.AliyunSmsSigner;
import wiki.chiu.micro.auth.support.VerificationCodeGenerator;
import wiki.chiu.micro.common.exception.CodeException;
import wiki.chiu.micro.common.lang.Const;

/**
 * @author mingchiuli
 * @create 2022-11-27 8:28 pm
 */
@Service
public class CodeServiceImpl implements CodeService {

  private final JavaMailSender javaMailSender;

  private final RedissonClient redissonClient;

  private final UserDirectory users;

  private final SmsSender smsSender;

  private final ResourceLoader resourceLoader;

  private final JsonMapper jsonMapper;

  @Value("${spring.mail.properties.from}")
  private String from;

  @Value("${megalith.blog.aliyun.access-key-id}")
  private String accessKeyId;

  @Value("${megalith.blog.aliyun.access-key-secret}")
  private String accessKeySecret;

  private String script;

  public CodeServiceImpl(
      JavaMailSender javaMailSender,
      RedissonClient redissonClient,
      UserDirectory users,
      SmsSender smsSender,
      ResourceLoader resourceLoader,
      JsonMapper jsonMapper) {
    this.javaMailSender = javaMailSender;
    this.redissonClient = redissonClient;
    this.users = users;
    this.smsSender = smsSender;
    this.resourceLoader = resourceLoader;
    this.jsonMapper = jsonMapper;
  }

  @PostConstruct
  private void init() throws IOException {
    Resource resource =
        resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/hmset-expire.lua");
    script = resource.getContentAsString(StandardCharsets.UTF_8);
  }

  @Override
  public void createEmailCode(String loginEmail) {
    validateUserEmail(loginEmail);
    String key = Const.EMAIL_KEY + loginEmail;
    checkCodeExistence(key);

    Object code = VerificationCodeGenerator.generate(Const.EMAIL_CODE);
    sendEmail(loginEmail, code.toString());
    saveCodeToRedis(key, code.toString());
  }

  @Override
  public void createSMSCode(String loginSMS) {
    validateUserPhone(loginSMS);
    String key = Const.PHONE_KEY + loginSMS;
    checkCodeExistence(key);

    Object code = VerificationCodeGenerator.generate(SMS_CODE);
    sendSms(loginSMS, code);
    saveCodeToRedis(key, code.toString());
  }

  private void validateUserEmail(String email) {
    users.findByEmail(email);
  }

  private void validateUserPhone(String phone) {
    users.findByPhone(phone);
  }

  private void checkCodeExistence(String key) {
    if (redissonClient.getBucket(key).isExists()) {
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
    String signature =
        AliyunSmsSigner.getSignature(
            phone, jsonMapper.writeValueAsString(codeMap), accessKeyId, accessKeySecret);
    smsSender.send("?Signature=" + signature);
  }

  private void saveCodeToRedis(String key, String code) {
    redissonClient
        .getScript()
        .eval(
            RScript.Mode.READ_WRITE,
            script,
            RScript.ReturnType.VALUE,
            Collections.singletonList(key),
            "code",
            code,
            "try_count",
            "0",
            "120");
  }
}
