package wiki.chiu.micro.auth.application.service;

import static wiki.chiu.micro.common.lang.Const.SMS_CODE;
import static wiki.chiu.micro.common.lang.ExceptionMessage.CODE_EXISTED;

import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.auth.application.port.in.CodeService;
import wiki.chiu.micro.auth.application.port.out.SmsSender;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;
import wiki.chiu.micro.auth.application.port.out.VerificationCodeStore;
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

  private final VerificationCodeStore codes;

  private final UserDirectory users;

  private final SmsSender smsSender;

  private final JsonMapper jsonMapper;

  @Value("${spring.mail.properties.from}")
  private String from;

  @Value("${megalith.blog.aliyun.access-key-id}")
  private String accessKeyId;

  @Value("${megalith.blog.aliyun.access-key-secret}")
  private String accessKeySecret;

  public CodeServiceImpl(
      JavaMailSender javaMailSender,
      VerificationCodeStore codes,
      UserDirectory users,
      SmsSender smsSender,
      JsonMapper jsonMapper) {
    this.javaMailSender = javaMailSender;
    this.codes = codes;
    this.users = users;
    this.smsSender = smsSender;
    this.jsonMapper = jsonMapper;
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
    if (codes.exists(key)) {
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
    codes.save(key, code);
  }
}
