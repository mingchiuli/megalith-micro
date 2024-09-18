package org.chiu.micro.blog.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionMessage {

    NO_FOUND(1, "没有找到内容"),

    UPLOAD_MISS(3, "上传出现错误"),

    USER_MISS(4, "用户没有找到"),

    EDIT_NO_AUTH(5, "必须编辑自己的文章");

    private final Integer code;

    private final String msg;

}
