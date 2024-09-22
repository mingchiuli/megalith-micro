package org.chiu.micro.websocket.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BlogEditPushActionReq(
        Long id,
        //内容变动的部分
        String contentChange,

        @NotNull
        Integer operateTypeCode,

        @NotNull
        Integer version,

        Integer indexStart,

        Integer indexEnd,

        @NotBlank
        String field,

        Integer paraNo) {
}
