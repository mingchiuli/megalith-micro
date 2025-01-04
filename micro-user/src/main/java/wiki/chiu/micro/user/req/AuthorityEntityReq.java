package wiki.chiu.micro.user.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import wiki.chiu.micro.user.valid.ListValue;

import java.util.Optional;


public record AuthorityEntityReq(

        Optional<Long> id,

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

        @ListValue(values = {0, 1})
        Integer type,

        @ListValue(values = {0, 1})
        Integer status) {
}
