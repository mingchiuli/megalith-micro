package org.chiu.micro.user.convertor;

import org.chiu.micro.user.entity.RoleEntity;
import org.chiu.micro.user.req.RoleEntityReq;

public class RoleEntityConvertor {
    public static void convert(RoleEntityReq roleReq, RoleEntity roleEntity) {
        roleEntity.setId(roleReq.id());
        roleEntity.setCode(roleReq.code());
        roleEntity.setName(roleReq.name());
        roleEntity.setStatus(roleReq.status());
        roleEntity.setRemark(roleReq.remark());
    }
}
