package org.chiu.micro.auth.convertor;

import java.util.List;

import org.chiu.micro.auth.dto.AuthorityDto;
import org.chiu.micro.auth.vo.AuthorityVo;

public class AuthorityVoConvertor {

    private AuthorityVoConvertor() {}

    public static List<AuthorityVo> convert(List<AuthorityDto> systemAuthorities) {
        return systemAuthorities.stream()
                .map(item -> AuthorityVo.builder()
                        .id(item.getId())
                        .code(item.getCode())
                        .methodType(item.getMethodType())
                        .prototype(item.getPrototype())
                        .remark(item.getRemark())
                        .routePattern(item.getRoutePattern())
                        .serviceHost(item.getServiceHost())
                        .servicePort(item.getServicePort())
                        .status(item.getStatus())
                        .name(item.getName())
                        .build())
                .toList();
    }
}
