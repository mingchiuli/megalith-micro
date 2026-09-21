package wiki.chiu.micro.common.outbox.application;

import wiki.chiu.micro.common.outbox.config.OutboxProperties;

public final class OutboxLockNames {

    private OutboxLockNames() {
    }

    public static String publisher(OutboxProperties properties) {
        return "outbox:publisher:" + properties.getProducer().name();
    }
}
