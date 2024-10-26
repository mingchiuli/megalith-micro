package wiki.chiu.micro.common.dto;


public record AuthorityRouteRpcDto(

        Boolean auth,

        String serviceHost,

        Integer servicePort) {
}
