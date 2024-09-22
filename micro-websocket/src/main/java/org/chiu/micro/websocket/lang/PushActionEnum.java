package org.chiu.micro.websocket.lang;


public enum PushActionEnum {

    STATUS(-1, "Status操作"),

    NON_PARA_TAIL_APPEND(0, "从末尾向后添加"),

    NON_PARA_TAIL_SUBTRACT(1, "从末尾向前减少"),

    NON_PARA_HEAD_APPEND(2, "从开头增加"),

    NON_PARA_HEAD_SUBTRACT(3, "从开头减少"),

    NON_PARA_REPLACE(4, "替换文本"),

    NON_PARA_REMOVE(5, "删除所有"),

    PARA_TAIL_APPEND(6, "从末尾向后添加"),

    PARA_TAIL_SUBTRACT(7, "从末尾向前减少"),

    PARA_HEAD_APPEND(8, "从开头增加"),

    PARA_HEAD_SUBTRACT(9, "从开头减少"),

    PARA_REPLACE(10, "替换文本"),

    PARA_REMOVE(11, "删除所有"),

    PARA_SPLIT_APPEND(12, "增加一段"),

    PARA_SPLIT_SUBTRACT(13, "减少一段"),

    SENSITIVE_CONTENT_LIST(14, "敏感词操作");

    private final Integer code;

    private final String description;

    PushActionEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PushActionEnum getInstance(Integer code) {
        for (PushActionEnum value : PushActionEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException();
    }

    public Integer getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }
}
