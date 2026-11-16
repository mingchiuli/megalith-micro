package wiki.chiu.micro.auth.application.service;

import static wiki.chiu.micro.common.constant.Const.EMAIL_CODE;
import static wiki.chiu.micro.common.constant.Const.SMS_CODE;

import java.util.UUID;

import wiki.chiu.micro.auth.application.port.in.CodeService;
import wiki.chiu.micro.auth.application.port.out.LoginCodeStore;
import wiki.chiu.micro.auth.application.port.out.MailSender;
import wiki.chiu.micro.auth.application.port.out.SmsSender;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;
import wiki.chiu.micro.auth.domain.VerificationCodeGenerator;
import wiki.chiu.micro.common.error.ExceptionMessage;
import wiki.chiu.micro.common.exception.CodeException;

/**
 * @author mingchiuli
 * @create 2022-11-27 8:28 pm
 */
public class CodeServiceImpl implements CodeService {

    private final MailSender mails;

    private final LoginCodeStore codes;

    private final UserDirectory users;

    private final SmsSender smsSender;

    public CodeServiceImpl(
        MailSender mails, LoginCodeStore codes, UserDirectory users, SmsSender smsSender) {
        this.mails = mails;
        this.codes = codes;
        this.users = users;
        this.smsSender = smsSender;
    }

    @Override
    public void createEmailCode(String loginEmail) {
        users.findByEmail(loginEmail);
        send(LoginCodeStore.LoginChannel.EMAIL, loginEmail,
            VerificationCodeGenerator.generate(EMAIL_CODE), mails::sendLoginCode);
    }

    @Override
    public void createSMSCode(String loginSMS) {
        users.findByPhone(loginSMS);
        send(LoginCodeStore.LoginChannel.PHONE, loginSMS,
            VerificationCodeGenerator.generate(SMS_CODE), smsSender::sendLoginCode);
    }

    private void send(LoginCodeStore.LoginChannel channel, String principal, String code,
                      java.util.function.BiConsumer<String, String> sender) {
        String reservation = UUID.randomUUID().toString();
        if (!codes.reserve(channel, principal, code, reservation)) {
            throw new CodeException(ExceptionMessage.CODE_EXISTED);
        }
        try {
            sender.accept(principal, code);
        } catch (RuntimeException failure) {
            try {
                codes.cancel(channel, principal, reservation);
            } catch (RuntimeException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

}
