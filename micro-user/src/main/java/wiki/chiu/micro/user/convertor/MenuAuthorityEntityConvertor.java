package wiki.chiu.micro.user.convertor;

import java.util.List;
import wiki.chiu.micro.user.entity.MenuAuthorityEntity;

public class MenuAuthorityEntityConvertor {

  private MenuAuthorityEntityConvertor() {}

  public static List<MenuAuthorityEntity> convert(Long menuId, List<Long> authorityIds) {
    return authorityIds.stream()
        .map(
            authorityId ->
                MenuAuthorityEntity.builder().authorityId(authorityId).menuId(menuId).build())
        .toList();
  }
}
