package org.chiu.micro.gateway.req;


import lombok.Data;

/**
 * @author mingchiuli
 * @create 2022-12-06 8:57 pm
 */
@Data
public class RoleEntityReq {

    private Long id;

    private String name;

    private String code;

    private String remark;

    private Integer status;
}
