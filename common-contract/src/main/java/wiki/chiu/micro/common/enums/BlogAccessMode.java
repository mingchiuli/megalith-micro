package wiki.chiu.micro.common.enums;

public enum BlogAccessMode {
    PUBLIC(0),
    RESTRICTED(1);

    private final int code;

    BlogAccessMode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static BlogAccessMode of(Integer code) {
        return code != null && code == RESTRICTED.code ? RESTRICTED : PUBLIC;
    }
}
