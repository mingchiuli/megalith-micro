package org.chiu.micro.auth.dto;


public record AuthorityDto (

        Long id,

        String code,

        String remark,

        String prototype,

        String methodType,

        String routePattern,

        String serviceHost,

        Integer servicePort,

        Integer type,

        Integer status) {
}
