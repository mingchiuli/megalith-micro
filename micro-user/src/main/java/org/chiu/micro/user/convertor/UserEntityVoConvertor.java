package org.chiu.micro.user.convertor;

import org.chiu.micro.user.page.PageAdapter;
import org.chiu.micro.user.entity.UserEntity;
import org.chiu.micro.user.vo.UserEntityVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

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

    public static PageAdapter<UserEntityVo> convert(Page<UserEntity> page, Map<Long, List<String>> userIdRoleMap) {
        List<UserEntityVo> content = page.getContent().stream()
                .map(user -> UserEntityVo.builder()
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .updated(user.getUpdated())
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
