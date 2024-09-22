package org.chiu.micro.auth.convertor;

import java.util.List;

import org.chiu.micro.auth.dto.AuthorityDto;
import org.chiu.micro.auth.vo.AuthorityVo;

public class AuthorityVoConvertor {

    private AuthorityVoConvertor() {}

    public static List<AuthorityVo> convert(List<AuthorityDto> systemAuthorities) {
        return systemAuthorities.stream()
                .map(item -> AuthorityVo.builder()
                        .id(item.id())
                        .code(item.code())
                        .methodType(item.methodType())
                        .prototype(item.prototype())
                        .remark(item.remark())
                        .routePattern(item.routePattern())
                        .serviceHost(item.serviceHost())
                        .servicePort(item.servicePort())
                        .status(item.status())
                        .name(item.name())
                        .build())
                .toList();
    }
}
