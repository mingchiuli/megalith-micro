package wiki.chiu.micro.user.req;

import jakarta.validation.constraints.NotBlank;
import wiki.chiu.micro.user.valid.ListValue;

import java.util.Optional;

/**
 * @author mingchiuli
 * @create 2022-12-06 8:57 pm
 */
public record RoleEntityReq(

        Optional<Long> id,

        @NotBlank
        String name,

        @NotBlank
        String code,

        String remark,

        @ListValue(values = {0, 1})
        Integer status) {
}
