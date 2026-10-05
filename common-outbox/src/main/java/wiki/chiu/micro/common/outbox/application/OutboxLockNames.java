package wiki.chiu.micro.common.outbox.application;

public final class OutboxLockNames {

    private OutboxLockNames() {
    }

    public static String publisher(String producerName) {
        return "outbox:publisher:" + producerName;
    }
}
