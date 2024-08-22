package org.chiu.micro.websocket.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BlogEditPushActionReq {

    private Long id;

    //内容变动的部分
    private String contentChange;

    @NotNull
    private Integer operateTypeCode;

    @NotNull
    private Integer version;

    private Integer indexStart;

    private Integer indexEnd;

    @NotBlank
    private String field;

    private Integer paraNo;
}
