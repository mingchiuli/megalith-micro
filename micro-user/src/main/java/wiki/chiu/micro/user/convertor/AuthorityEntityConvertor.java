package wiki.chiu.micro.user.convertor;

import wiki.chiu.micro.user.entity.AuthorityEntity;
import wiki.chiu.micro.user.req.AuthorityEntityReq;

public class AuthorityEntityConvertor {

    public static void convert(AuthorityEntityReq req, AuthorityEntity authorityEntity) {
        authorityEntity.setId(req.id().orElse(null));
        authorityEntity.setCode(req.code());
        authorityEntity.setRemark(req.remark());
        authorityEntity.setPrototype(req.prototype());
        authorityEntity.setMethodType(req.methodType());
        authorityEntity.setRoutePattern(req.routePattern());
        authorityEntity.setServiceHost(req.serviceHost());
        authorityEntity.setServicePort(req.servicePort());
        authorityEntity.setType(req.type());
        authorityEntity.setStatus(req.status());
    }
}
