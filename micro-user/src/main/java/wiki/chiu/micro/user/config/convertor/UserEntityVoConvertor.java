package wiki.chiu.micro.user.config.convertor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.domain.RoleEntity;
import wiki.chiu.micro.user.domain.UserEntity;
import wiki.chiu.micro.user.domain.UserRoleEntity;
import wiki.chiu.micro.user.vo.UserEntityVo;

public class UserEntityVoConvertor {

    private UserEntityVoConvertor() {
    }

    public static UserEntityVo convert(UserEntity userEntity, List<String> roleCodes) {
        return UserEntityVo.builder()
            .id(userEntity.getId())
            .username(userEntity.getUsername())
            .nickname(userEntity.getNickname())
            .avatar(userEntity.getAvatar())
            .email(userEntity.getEmail())
            .phone(userEntity.getPhone())
            .status(userEntity.getStatus())
            .created(userEntity.getCreated())
            .lastLogin(userEntity.getLastLogin())
            .roles(roleCodes)
            .build();
    }

    public static PageAdapter<UserEntityVo> convert(
        PageAdapter<UserEntity> page,
        List<UserRoleEntity> userRoleEntities,
        List<RoleEntity> roleEntities) {

        Map<Long, List<String>> userIdRoleMap =
            userRoleEntities.stream()
                .collect(Collectors.groupingBy(UserRoleEntity::getUserId))
                .entrySet()
                .stream()
                .map(
                    entry -> {
                        List<Long> roleIds =
                            entry.getValue().stream().map(UserRoleEntity::getRoleId).toList();
                        List<String> roleCodes =
                            roleEntities.stream()
                                .filter(item -> roleIds.contains(item.getId()))
                                .map(RoleEntity::getCode)
                                .toList();
                        return Map.entry(entry.getKey(), roleCodes);
                    })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<Long, LocalDateTime> userDate =
            page.content().stream().collect(Collectors.toMap(UserEntity::getId, UserEntity::getUpdated));

        Map<Long, LocalDateTime> userRoleDate =
            userRoleEntities.stream()
                .collect(
                    Collectors.toMap(
                        UserRoleEntity::getUserId,
                        UserRoleEntity::getUpdated,
                        (v1, v2) -> v1.isAfter(v2) ? v1 : v2));

        Map<Long, LocalDateTime> mergedMap =
            Stream.of(userRoleDate, userDate)
                .flatMap(map -> map.entrySet().stream())
                .collect(
                    HashMap::new,
                    (m, e) -> m.merge(e.getKey(), e.getValue(), (v1, v2) -> v1.isAfter(v2) ? v1 : v2),
                    HashMap::putAll);

        List<UserEntityVo> content =
            page.content().stream()
                .map(
                    user ->
                        UserEntityVo.builder()
                            .email(user.getEmail())
                            .phone(user.getPhone())
                            .updated(mergedMap.get(user.getId()))
                            .id(user.getId())
                            .nickname(user.getNickname())
                            .status(user.getStatus())
                            .avatar(user.getAvatar())
                            .created(user.getCreated())
                            .lastLogin(user.getLastLogin())
                            .username(user.getUsername())
                            .roles(userIdRoleMap.get(user.getId()))
                            .build())
                .toList();

        return PageAdapter.<UserEntityVo>builder()
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
}
