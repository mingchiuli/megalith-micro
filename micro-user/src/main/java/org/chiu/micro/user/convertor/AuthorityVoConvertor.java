package org.chiu.micro.user.convertor;

import org.chiu.micro.user.entity.AuthorityEntity;
import org.chiu.micro.user.vo.AuthorityVo;

import java.util.List;

public class AuthorityVoConvertor {

    private AuthorityVoConvertor() {
    }

    public static List<AuthorityVo> convert(List<AuthorityEntity> authorityEntities) {
        return authorityEntities.stream()
                .map(item -> AuthorityVo.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .code(item.getCode())
                        .prototype(item.getPrototype())
                        .methodType(item.getMethodType())
                        .serviceHost(item.getServiceHost())
                        .servicePort(item.getServicePort())
                        .routePattern(item.getRoutePattern())
                        .created(item.getCreated())
                        .updated(item.getUpdated())
                        .status(item.getStatus())
                        .remark(item.getRemark())
                        .build())
                .toList();
    }

    public static AuthorityVo convert(AuthorityEntity authorityEntity) {
        return AuthorityVo.builder()
                .id(authorityEntity.getId())
                .name(authorityEntity.getName())
                .code(authorityEntity.getCode())
                .prototype(authorityEntity.getPrototype())
                .methodType(authorityEntity.getMethodType())
                .serviceHost(authorityEntity.getServiceHost())
                .servicePort(authorityEntity.getServicePort())
                .routePattern(authorityEntity.getRoutePattern())
                .created(authorityEntity.getCreated())
                .updated(authorityEntity.getUpdated())
                .status(authorityEntity.getStatus())
                .remark(authorityEntity.getRemark())
                .build();
    }
}
