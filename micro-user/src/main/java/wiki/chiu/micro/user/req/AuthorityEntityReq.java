package wiki.chiu.micro.user.req;

import java.util.Optional;

public record AuthorityEntityReq(
    Optional<Long> id,
    String code,
    String remark,
    String prototype,
    String methodType,
    String routePattern,
    String serviceHost,
    Integer servicePort,
    Integer type,
    Integer status) {}
