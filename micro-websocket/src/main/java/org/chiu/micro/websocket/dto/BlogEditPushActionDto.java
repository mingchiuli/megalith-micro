package org.chiu.micro.websocket.dto;

import java.io.Serializable;

public record BlogEditPushActionDto(
        Long id,
        //内容变动的部分
        String contentChange,

        Integer operateTypeCode,

        Integer version,

        Integer indexStart,

        Integer indexEnd,

        String field,

        Integer paraNo) implements Serializable {
}
