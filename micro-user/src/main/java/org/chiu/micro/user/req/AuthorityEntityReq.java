package org.chiu.micro.user.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class AuthorityEntityReq {

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String code;

    @NotBlank
    private String remark;

    @NotBlank
    private String prototype;

    @NotBlank
    private String methodType;

    @NotBlank
    private String routePattern;

    @NotBlank
    private String serviceName;

    @NotNull
    private Integer status;
}
