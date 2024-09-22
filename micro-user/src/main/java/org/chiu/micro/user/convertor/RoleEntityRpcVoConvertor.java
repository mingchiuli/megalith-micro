package org.chiu.micro.user.convertor;

import org.chiu.micro.user.entity.RoleEntity;
import org.chiu.micro.user.vo.RoleEntityRpcVo;

import java.util.List;

public class RoleEntityRpcVoConvertor {

    private RoleEntityRpcVoConvertor() {
    }

    public static List<RoleEntityRpcVo> convert(List<RoleEntity> entities) {

        return entities.stream()
                .map(item -> RoleEntityRpcVo.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .code(item.getCode())
                        .remark(item.getRemark())
                        .status(item.getStatus())
                        .created(item.getCreated())
                        .updated(item.getUpdated())
                        .build())
                .toList();
    }

}
