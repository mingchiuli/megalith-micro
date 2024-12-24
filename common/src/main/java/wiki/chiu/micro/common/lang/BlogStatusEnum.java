package wiki.chiu.micro.common.lang;


import java.util.Arrays;
import java.util.List;

public enum BlogStatusEnum {

    NORMAL(0, "正常状态"),

    HIDE(1, "隐藏/禁用状态"),

    SENSITIVE_FILTER(2, "过滤敏感内容状态");

    private final Integer code;

    private final String description;

    BlogStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public static final List<Integer> BLOG_STATUS_SET = Arrays.stream(BlogStatusEnum.values())
            .map(BlogStatusEnum::getCode)
            .toList();

}
