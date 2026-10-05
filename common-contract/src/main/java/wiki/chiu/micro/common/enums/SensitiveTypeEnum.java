package wiki.chiu.micro.common.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    public static final List<Integer> SENSITIVE_TYPE_SET =
        Arrays.stream(SensitiveTypeEnum.values()).map(SensitiveTypeEnum::getCode).toList();

    /**
     * Resolves a wire code to its field. Unknown codes have no field to mask, and callers skip
     * them.
     *
     * @param code the code carried by a sensitive content payload
     * @return the matching field, or empty when the code is not a field this service knows
     */
    public static Optional<SensitiveTypeEnum> ofCode(Integer code) {
        return Arrays.stream(SensitiveTypeEnum.values())
            .filter(type -> type.getCode().equals(code))
            .findFirst();
    }
}
