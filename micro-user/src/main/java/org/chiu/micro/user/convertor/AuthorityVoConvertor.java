package org.chiu.micro.user.convertor;

import org.chiu.micro.user.entity.AuthorityEntity;
import org.chiu.micro.user.vo.AuthorityVo;

import java.util.ArrayList;
import java.util.List;

public class AuthorityVoConvertor {

    private AuthorityVoConvertor() {}

    public static List<AuthorityVo> convert(List<AuthorityEntity> authorityEntities) {
        List<AuthorityVo> vos = new ArrayList<>();
        authorityEntities.forEach(item -> vos
                .add(AuthorityVo.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .code(item.getCode())
                        .created(item.getCreated())
                        .updated(item.getUpdated())
                        .status(item.getStatus())
                        .remark(item.getRemark())
                        .build()));
        return vos;
    }

    public static AuthorityVo convert(AuthorityEntity authorityEntity) {
        return AuthorityVo.builder()
                .id(authorityEntity.getId())
                .name(authorityEntity.getName())
                .code(authorityEntity.getCode())
                .created(authorityEntity.getCreated())
                .updated(authorityEntity.getUpdated())
                .status(authorityEntity.getStatus())
                .remark(authorityEntity.getRemark())
                .build();
    }
}
