package org.chiu.micro.user.req;

import org.chiu.micro.user.valid.ListValue;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author mingchiuli
 * @create 2022-12-06 8:57 pm
 */
@Data
public class RoleEntityReq {

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String code;

    private String remark;

    @ListValue(values = {0, 1})
    private Integer status;
}
