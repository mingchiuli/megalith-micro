package org.chiu.micro.user.convertor;

import org.chiu.micro.user.page.PageAdapter;
import org.chiu.micro.user.entity.RoleEntity;
import org.chiu.micro.user.entity.UserEntity;
import org.chiu.micro.user.vo.UserEntityVo;
import org.chiu.micro.user.entity.UserRoleEntity;

import org.springframework.data.domain.Page;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;


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

    public static PageAdapter<UserEntityVo> convert(Page<UserEntity> page, List<UserRoleEntity> userRoleEntities, List<RoleEntity> roleEntities) {
        
        Map<Long, List<UserRoleEntity>> idUserRoleMap = userRoleEntities.stream()
                .collect(Collectors.groupingBy(UserRoleEntity::getUserId));
        
        Map<Long, List<String>> userIdRoleMap = idUserRoleMap.entrySet().stream()
                .map(entry -> {
                    List<Long> roleIds = entry.getValue().stream()
                            .map(UserRoleEntity::getRoleId)
                            .toList();
                    List<String> roleCodes = roleEntities.stream()
                            .filter(item -> roleIds.contains(item.getId()))
                            .map(RoleEntity::getCode)
                            .toList();
                    return Map.entry(entry.getKey(), roleCodes);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        
        Map<Long, LocalDateTime> idDateMap = idUserRoleMap.entrySet().stream()
                .map(entry -> {
                    var date = entry.getValue().stream()
                            .sorted(Comparator.comparing(UserRoleEntity::getUpdated).reversed())
                            .findFirst()
                            .get()
                            .getUpdated();
                    return Map.entry(entry.getKey(), date);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        
        
        List<UserEntityVo> content = page.getContent().stream()
                .map(user -> UserEntityVo.builder()
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .updated(user.getUpdated().isAfter(idDateMap.get(user.getId())) ? user.getUpdated() : idDateMap.get(user.getId()))
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
                .empty(page.isEmpty())
                .first(page.isFirst())
                .last(page.isLast())
                .pageNumber(page.getPageable().getPageNumber())
                .content(content)
                .totalPages(page.getTotalPages())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .build();
    }
}
