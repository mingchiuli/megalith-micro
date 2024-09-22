package org.chiu.micro.user.req;

import jakarta.validation.constraints.NotBlank;
import org.chiu.micro.user.valid.ListValue;

/**
 * @author mingchiuli
 * @create 2022-12-06 8:57 pm
 */
public record RoleEntityReq(

        Long id,

        @NotBlank
        String name,

        @NotBlank
        String code,

        String remark,

        @ListValue(values = {0, 1})
        Integer status) {
}
