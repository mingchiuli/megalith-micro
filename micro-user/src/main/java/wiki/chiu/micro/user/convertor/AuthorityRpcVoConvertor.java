package wiki.chiu.micro.user.convertor;

import wiki.chiu.micro.common.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.entity.AuthorityEntity;

import java.util.List;

public class AuthorityRpcVoConvertor {

    public static List<AuthorityRpcVo> convert(List<AuthorityEntity> authorityEntities) {
        return authorityEntities.stream()
                .map(item -> AuthorityRpcVo.builder()
                        .id(item.getId())
                        .code(item.getCode())
                        .prototype(item.getPrototype())
                        .methodType(item.getMethodType())
                        .serviceHost(item.getServiceHost())
                        .servicePort(item.getServicePort())
                        .routePattern(item.getRoutePattern())
                        .type(item.getType())
                        .status(item.getStatus())
                        .remark(item.getRemark())
                        .build())
                .toList();
    }
}

