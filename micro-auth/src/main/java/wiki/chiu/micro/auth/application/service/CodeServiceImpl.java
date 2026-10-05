package wiki.chiu.micro.auth.application.service;

import static wiki.chiu.micro.common.constant.Const.EMAIL_CODE;
import static wiki.chiu.micro.common.constant.Const.SMS_CODE;

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
        checkCodeExistence(LoginCodeStore.LoginChannel.EMAIL, loginEmail);

        String code = VerificationCodeGenerator.generate(EMAIL_CODE);
        mails.sendLoginCode(loginEmail, code);
        codes.save(LoginCodeStore.LoginChannel.EMAIL, loginEmail, code);
    }

    @Override
    public void createSMSCode(String loginSMS) {
        users.findByPhone(loginSMS);
        checkCodeExistence(LoginCodeStore.LoginChannel.PHONE, loginSMS);

        String code = VerificationCodeGenerator.generate(SMS_CODE);
        smsSender.sendLoginCode(loginSMS, code);
        codes.save(LoginCodeStore.LoginChannel.PHONE, loginSMS, code);
    }

    private void checkCodeExistence(LoginCodeStore.LoginChannel channel, String principal) {
        if (codes.exists(channel, principal)) {
            throw new CodeException(ExceptionMessage.CODE_EXISTED);
        }
    }
}
