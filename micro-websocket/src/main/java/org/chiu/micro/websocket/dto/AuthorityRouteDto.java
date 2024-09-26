package org.chiu.micro.websocket.dto;


public record AuthorityRouteDto(

        Boolean auth,

        String serviceHost,

        Integer servicePort) {
}
