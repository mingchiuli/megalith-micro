package wiki.chiu.micro.common.dto;


import java.io.Serializable;

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

        Integer status) implements Serializable {
}
