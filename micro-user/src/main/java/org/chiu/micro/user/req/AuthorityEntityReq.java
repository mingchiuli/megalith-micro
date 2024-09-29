package org.chiu.micro.user.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;


public record AuthorityEntityReq(

        Optional<Long> id,

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

        @NotNull
        Integer servicePort,

        @NotNull
        Integer type,

        @NotNull
        Integer status) {
}
