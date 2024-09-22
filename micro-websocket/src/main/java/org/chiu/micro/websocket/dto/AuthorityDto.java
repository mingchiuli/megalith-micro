package org.chiu.micro.websocket.dto;


public record AuthorityDto(

        Long id,

        String name,

        String code,

        String remark,

        String prototype,

        String methodType,

        String routePattern,

        String serviceHost,

        Integer servicePort,

        Integer status) {
}
