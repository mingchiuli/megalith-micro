package wiki.chiu.micro.common.enums;

public enum AuthTypeEnum {
    WHITE_LIST(0, "白名单"),

    NEED_AUTH(1, "需鉴权");

    private final Integer code;

    AuthTypeEnum(Integer code, String description) {
        this.code = code;
    }

    public Integer getCode() {
        return this.code;
    }
}
