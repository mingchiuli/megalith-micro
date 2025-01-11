package wiki.chiu.micro.user.convertor;

import wiki.chiu.micro.user.entity.MenuAuthorityEntity;

import java.util.List;

public class MenuAuthorityEntityConvertor {

    private MenuAuthorityEntityConvertor() {
    }

    public static List<MenuAuthorityEntity> convert(Long menuId, List<Long> authorityIds) {
        return authorityIds.stream()
                .map(authorityId -> MenuAuthorityEntity.builder()
                        .authorityId(authorityId)
                        .menuId(menuId)
                        .build())
                .toList();
    }
}
