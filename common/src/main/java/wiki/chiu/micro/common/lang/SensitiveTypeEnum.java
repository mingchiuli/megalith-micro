package wiki.chiu.micro.common.lang;


import java.util.Arrays;
import java.util.List;

public enum SensitiveTypeEnum {

    TITLE(1, "title"),

    DESCRIPTION(2, "description"),

    CONTENT(3, "content");

    private final Integer code;

    private final String description;

    SensitiveTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public static final List<Integer> SENSITIVE_TYPE_SET = Arrays.stream(SensitiveTypeEnum.values())
            .map(SensitiveTypeEnum::getCode)
            .toList();
}
