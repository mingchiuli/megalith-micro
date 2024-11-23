package wiki.chiu.micro.common.utils;

import wiki.chiu.micro.common.exception.CodeException;

import java.util.Random;

import static wiki.chiu.micro.common.lang.Const.*;

/**
 * @author mingchiuli
 * @create 2023-03-05 1:04 am
 */
public class CodeUtils {

    private static final char[] code = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z',
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
    };

    private static final char[] sms = {
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
    };

    private static final Random random = new Random();

    public static String create(String type) {
        if (SMS_CODE.equals(type)) {
            return createSMS();
        } else if (EMAIL_CODE.equals(type)) {
            return createEmailCode();
        } else if (PHONE_CODE.equals(type)) {
            return createPhone();
        }
        throw new CodeException("code type input error");
    }

    private static String createEmailCode() {
        var builder = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            int idx = random.nextInt(code.length);
            builder.append(code[idx]);
        }
        return builder.toString();
    }

    private static String createSMS() {
        var builder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int idx = random.nextInt(sms.length);
            builder.append(sms[idx]);
        }
        return builder.toString();
    }

    public static String createPhone() {
        var builder = new StringBuilder();
        builder.append(2);
        for (int i = 0; i < 10; i++) {
            int idx = random.nextInt(sms.length);
            builder.append(sms[idx]);
        }
        return builder.toString();
    }
}
