package wiki.chiu.micro.common.dto;


public record AuthorityRpcDto(

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
