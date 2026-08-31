package wiki.chiu.micro.auth.support;

import static wiki.chiu.micro.common.lang.Const.EMAIL_CODE;
import static wiki.chiu.micro.common.lang.Const.SMS_CODE;

import java.util.concurrent.ThreadLocalRandom;

import wiki.chiu.micro.common.exception.CodeException;

/**
 * @author mingchiuli
 * @create 2023-03-05 1:04 am
 */
public final class VerificationCodeGenerator {

    private static final char[] code = {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
        't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
    };

    private static final char[] sms = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

    private VerificationCodeGenerator() {
    }

    public static String generate(String type) {
        if (SMS_CODE.equals(type)) {
            return createSMS();
        } else if (EMAIL_CODE.equals(type)) {
            return createEmailCode();
        }
        throw new CodeException("code type input error");
    }

    private static String createEmailCode() {
        var builder = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            int idx = ThreadLocalRandom.current().nextInt(code.length);
            builder.append(code[idx]);
        }
        return builder.toString();
    }

    private static String createSMS() {
        var builder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int idx = ThreadLocalRandom.current().nextInt(sms.length);
            builder.append(sms[idx]);
        }
        return builder.toString();
    }
}
