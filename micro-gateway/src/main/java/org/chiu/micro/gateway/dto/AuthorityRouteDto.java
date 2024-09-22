package org.chiu.micro.gateway.dto;


public record AuthorityRouteDto(

        Boolean auth,

        String serviceHost,

        Integer servicePort) {
}
