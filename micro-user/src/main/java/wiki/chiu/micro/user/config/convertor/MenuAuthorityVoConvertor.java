package wiki.chiu.micro.user.config.convertor;

import java.util.List;

import wiki.chiu.micro.user.domain.AuthorityEntity;
import wiki.chiu.micro.user.vo.MenuAuthorityVo;

public class MenuAuthorityVoConvertor {
    public static MenuAuthorityVo convert(AuthorityEntity authorityEntity, List<Long> ids) {
        return MenuAuthorityVo.builder()
            .authorityId(authorityEntity.getId())
            .code(authorityEntity.getCode())
            .check(ids.contains(authorityEntity.getId()))
            .build();
    }
}
