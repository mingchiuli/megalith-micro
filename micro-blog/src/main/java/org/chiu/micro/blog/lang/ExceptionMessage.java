package org.chiu.micro.blog.lang;


public enum ExceptionMessage {

    NO_FOUND(1, "没有找到内容"),

    UPLOAD_MISS(3, "上传出现错误"),

    USER_MISS(4, "用户没有找到"),

    EDIT_NO_AUTH(5, "必须编辑自己的文章");

    private final Integer code;

    private final String msg;

    ExceptionMessage(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }
}
