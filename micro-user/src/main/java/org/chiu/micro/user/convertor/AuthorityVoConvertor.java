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
                        .prototype(item.getPrototype())
                        .methodType(item.getMethodType())
                        .serviceName(item.getServiceName())
                        .servicePort(item.getServicePort())
                        .routePattern(item.getRoutePattern())
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
                .prototype(authorityEntity.getPrototype())
                .methodType(authorityEntity.getMethodType())
                .serviceName(authorityEntity.getServiceName())
                .servicePort(authorityEntity.getServicePort())
                .routePattern(authorityEntity.getRoutePattern())
                .created(authorityEntity.getCreated())
                .updated(authorityEntity.getUpdated())
                .status(authorityEntity.getStatus())
                .remark(authorityEntity.getRemark())
                .build();
    }
}
