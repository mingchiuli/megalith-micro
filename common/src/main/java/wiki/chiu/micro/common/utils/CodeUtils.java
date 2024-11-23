package wiki.chiu.micro.common.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * @author mingchiuli
 * @create 2023-03-05 1:04 am
 */
@Component
public class CodeUtils {

    private final Random random = new Random();


    private static final char[] sms = {
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
    };

    public CodeUtils() {
    }

    public String createPhone() {
        var builder = new StringBuilder();
        builder.append(2);
        for (int i = 0; i < 10; i++) {
            int idx = random.nextInt(sms.length);
            builder.append(sms[idx]);
        }
        return builder.toString();
    }
}
