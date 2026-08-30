package wiki.chiu.micro.user.config.convertor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.domain.RoleDataPermissionEntity;
import wiki.chiu.micro.user.domain.RoleEntity;
import wiki.chiu.micro.user.domain.RoleMenuEntity;
import wiki.chiu.micro.user.vo.RoleEntityVo;

public class RoleEntityVoConvertor {

  private RoleEntityVoConvertor() {}

  public static RoleEntityVo convert(
      RoleEntity roleEntity, List<RoleDataPermissionEntity> dataPermissions) {
    return RoleEntityVo.builder()
        .code(roleEntity.getCode())
        .name(roleEntity.getName())
        .remark(roleEntity.getRemark())
        .status(roleEntity.getStatus())
        .id(roleEntity.getId())
        .dataPermissions(
            dataPermissions.stream().map(RoleDataPermissionEntity::permission).sorted().toList())
        .build();
  }

  public static PageAdapter<RoleEntityVo> convert(
      PageAdapter<RoleEntity> page,
      List<RoleMenuEntity> roleMenus,
      List<RoleDataPermissionEntity> dataPermissions) {

    Map<Long, LocalDateTime> roleMenusDate =
        roleMenus.stream()
            .collect(
                Collectors.toMap(
                    RoleMenuEntity::getRoleId,
                    RoleMenuEntity::getUpdated,
                    (v1, v2) -> v1.isAfter(v2) ? v1 : v2));

    Map<Long, LocalDateTime> roleDate =
        page.content().stream().collect(Collectors.toMap(RoleEntity::getId, RoleEntity::getUpdated));

    Map<Long, LocalDateTime> roleDataPermissionDate =
        dataPermissions.stream()
            .collect(
                Collectors.toMap(
                    RoleDataPermissionEntity::getRoleId,
                    RoleDataPermissionEntity::getUpdated,
                    (v1, v2) -> v1.isAfter(v2) ? v1 : v2));

    Map<Long, List<wiki.chiu.micro.common.lang.DataPermissionEnum>> permissionsByRole =
        dataPermissions.stream()
            .collect(
                Collectors.groupingBy(
                    RoleDataPermissionEntity::getRoleId,
                    Collectors.mapping(
                        RoleDataPermissionEntity::permission,
                        Collectors.collectingAndThen(
                            Collectors.toList(),
                            permissions -> permissions.stream().sorted().toList()))));

    Map<Long, LocalDateTime> mergedMap =
        Stream.of(roleMenusDate, roleDataPermissionDate, roleDate)
            .flatMap(map -> map.entrySet().stream())
            .collect(
                HashMap::new,
                (m, e) -> m.merge(e.getKey(), e.getValue(), (v1, v2) -> v1.isAfter(v2) ? v1 : v2),
                HashMap::putAll);

    List<RoleEntityVo> content =
        page.content().stream()
            .map(
                role ->
                    RoleEntityVo.builder()
                        .code(role.getCode())
                        .name(role.getName())
                        .remark(role.getRemark())
                        .status(role.getStatus())
                        .updated(mergedMap.get(role.getId()))
                        .created(role.getCreated())
                        .id(role.getId())
                        .dataPermissions(permissionsByRole.getOrDefault(role.getId(), List.of()))
                        .build())
            .toList();

    return PageAdapter.<RoleEntityVo>builder()
        .empty(page.empty())
        .first(page.first())
        .last(page.last())
        .pageNumber(page.pageNumber())
        .content(content)
        .totalPages(page.totalPages())
        .pageSize(page.pageSize())
        .totalElements(page.totalElements())
        .build();
  }

  public static List<RoleEntityVo> convert(List<RoleEntity> entities) {

    return entities.stream()
        .map(
            item ->
                RoleEntityVo.builder()
                    .code(item.getCode())
                    .id(item.getId())
                    .status(item.getStatus())
                    .name(item.getName())
                    .dataPermissions(List.of())
                    .build())
        .toList();
  }
}
