package org.chiu.micro.user.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record AuthorityEntityReq(

        Long id,

        @NotBlank
        String name,

        @NotBlank
        String code,

        @NotBlank
        String remark,

        @NotBlank
        String prototype,

        @NotBlank
        String methodType,

        @NotBlank
        String routePattern,

        @NotBlank
        String serviceHost,

        @NotBlank
        String servicePort,

        @NotNull
        Integer status) {
}
