package wiki.chiu.micro.common.enums;

public enum BlogRenderMode {
    NORMAL(0),
    SENSITIVE_FILTER(2);

    private final int code;

    BlogRenderMode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static BlogRenderMode of(Integer code) {
        return code != null && code == SENSITIVE_FILTER.code ? SENSITIVE_FILTER : NORMAL;
    }
}
