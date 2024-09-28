package org.chiu.micro.user.convertor;

import org.chiu.micro.user.entity.AuthorityEntity;
import org.chiu.micro.user.req.AuthorityEntityReq;

public class AuthorityEntityConvertor {

    public static void convert(AuthorityEntityReq req, AuthorityEntity authorityEntity) {
        authorityEntity.setId(req.id().orElse(null));
        authorityEntity.setCode(req.code());
        authorityEntity.setName(req.name());
        authorityEntity.setRemark(req.remark());
        authorityEntity.setPrototype(req.prototype());
        authorityEntity.setMethodType(req.methodType());
        authorityEntity.setRoutePattern(req.routePattern());
        authorityEntity.setServiceHost(req.serviceHost());
        authorityEntity.setServicePort(req.servicePort());
        authorityEntity.setStatus(req.status());
    }
}
