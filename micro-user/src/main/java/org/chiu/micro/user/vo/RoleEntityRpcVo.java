package org.chiu.micro.user.vo;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;


@Data
@Builder
public class RoleEntityRpcVo {

    private Long id;

    private String name;

    private String code;

    private String remark;

    private LocalDateTime created;

    private LocalDateTime updated;

    private Integer status;
}
