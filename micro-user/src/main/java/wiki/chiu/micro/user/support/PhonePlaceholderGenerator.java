package wiki.chiu.micro.user.support;

import java.util.concurrent.ThreadLocalRandom;

public final class PhonePlaceholderGenerator {

    private PhonePlaceholderGenerator() {
    }

    public static String generate() {
        StringBuilder value = new StringBuilder("2");
        for (int i = 0; i < 10; i++) {
            value.append(ThreadLocalRandom.current().nextInt(10));
        }
        return value.toString();
    }
}
