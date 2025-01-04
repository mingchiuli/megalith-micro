package wiki.chiu.micro.user.convertor;

import wiki.chiu.micro.user.entity.RoleEntity;
import wiki.chiu.micro.user.req.RoleEntityReq;

public class RoleEntityConvertor {
    public static RoleEntity convert(RoleEntityReq roleReq, RoleEntity dealRole) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(roleReq.id().orElse(null));
        roleEntity.setCode(roleReq.code());
        roleEntity.setName(roleReq.name());
        roleEntity.setStatus(roleReq.status());
        roleEntity.setRemark(roleReq.remark());

        roleEntity.setCreated(dealRole.getCreated());

        return roleEntity;
    }
}
