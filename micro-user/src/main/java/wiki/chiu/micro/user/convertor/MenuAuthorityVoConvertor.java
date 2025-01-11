package wiki.chiu.micro.user.convertor;

import wiki.chiu.micro.user.entity.AuthorityEntity;
import wiki.chiu.micro.user.vo.MenuAuthorityVo;

import java.util.List;

public class MenuAuthorityVoConvertor {
    public static MenuAuthorityVo convert(AuthorityEntity authorityEntity, List<Long> ids) {
        return MenuAuthorityVo.builder()
                .authorityId(authorityEntity.getId())
                .code(authorityEntity.getCode())
                .check(ids.contains(authorityEntity.getId()))
                .build();
    }
}
