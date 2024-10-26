package wiki.chiu.micro.user.convertor;

import wiki.chiu.micro.user.entity.RoleEntity;
import wiki.chiu.micro.user.req.RoleEntityReq;

public class RoleEntityConvertor {
    public static void convert(RoleEntityReq roleReq, RoleEntity roleEntity) {
        roleEntity.setId(roleReq.id().orElse(null));
        roleEntity.setCode(roleReq.code());
        roleEntity.setName(roleReq.name());
        roleEntity.setStatus(roleReq.status());
        roleEntity.setRemark(roleReq.remark());
    }
}
