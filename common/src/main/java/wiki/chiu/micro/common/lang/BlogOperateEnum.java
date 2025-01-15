package wiki.chiu.micro.common.lang;

import wiki.chiu.micro.common.exception.MissException;


/**
 * @author mingchiuli
 * @create 2022-12-01 10:44 pm
 */
public enum BlogOperateEnum {

    CREATE(1, "新增"),

    UPDATE(2, "编辑"),

    REMOVE(3, "删除");

    private final Integer code;

    BlogOperateEnum(Integer code, String description) {
        this.code = code;
    }

    public Integer getCode() {
        return this.code;
    }

    public static BlogOperateEnum of(Integer code) {
        for (BlogOperateEnum blogOperateEnum : values()) {
            if (blogOperateEnum.code.equals(code)) {
                return blogOperateEnum;
            }
        }
        throw new MissException(ExceptionMessage.NO_FOUND);
    }
}
