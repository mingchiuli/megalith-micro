package org.chiu.micro.user.convertor;

import java.util.List;
import java.util.ArrayList;

import org.chiu.micro.user.entity.RoleEntity;
import org.chiu.micro.user.vo.RoleEntityRpcVo;

public class RoleEntityRpcVoConvertor {

    private RoleEntityRpcVoConvertor() {}

    public static List<RoleEntityRpcVo> convert(List<RoleEntity> entities) {
        List<RoleEntityRpcVo> out = new ArrayList<>();

        entities.forEach(item -> out.add(RoleEntityRpcVo.builder()
                .id(item.getId())
                .name(item.getName())
                .code(item.getCode())
                .remark(item.getRemark())
                .status(item.getStatus())
                .created(item.getCreated())
                .updated(item.getUpdated())
                .build()));

        return out;
    }


}
