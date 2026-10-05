package wiki.chiu.micro.auth.domain;

import static wiki.chiu.micro.common.constant.Const.EMAIL_CODE;
import static wiki.chiu.micro.common.constant.Const.SMS_CODE;

import java.util.concurrent.ThreadLocalRandom;

import wiki.chiu.micro.common.exception.CodeException;

/**
 * The login code shapes this service hands out: five alphanumeric characters by e-mail and six
 * digits by SMS.
 *
 * @author mingchiuli
 * @create 2023-03-05 1:04 am
 */
public final class VerificationCodeGenerator {

    private static final char[] EMAIL_ALPHABET = {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
        't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
    };

    private static final char[] SMS_ALPHABET = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

    private static final int EMAIL_CODE_LENGTH = 5;

    private static final int SMS_CODE_LENGTH = 6;

    private VerificationCodeGenerator() {
    }

    public static String generate(String type) {
        if (SMS_CODE.equals(type)) {
            return random(SMS_ALPHABET, SMS_CODE_LENGTH);
        }
        if (EMAIL_CODE.equals(type)) {
            return random(EMAIL_ALPHABET, EMAIL_CODE_LENGTH);
        }
        throw new CodeException("code type input error");
    }

    private static String random(char[] alphabet, int length) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int idx = ThreadLocalRandom.current().nextInt(alphabet.length);
            builder.append(alphabet[idx]);
        }
        return builder.toString();
    }
}
