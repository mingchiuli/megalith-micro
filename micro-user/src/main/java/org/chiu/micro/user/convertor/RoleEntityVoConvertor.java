package org.chiu.micro.user.convertor;

import org.chiu.micro.user.page.PageAdapter;
import org.chiu.micro.user.entity.RoleEntity;
import org.chiu.micro.user.vo.RoleEntityVo;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public class RoleEntityVoConvertor {

    private RoleEntityVoConvertor() {}

    public static RoleEntityVo convert(RoleEntity roleEntity) {
        return RoleEntityVo.builder()
                .code(roleEntity.getCode())
                .name(roleEntity.getName())
                .remark(roleEntity.getRemark())
                .status(roleEntity.getStatus())
                .id(roleEntity.getId())
                .build();
    }

    public static PageAdapter<RoleEntityVo> convert(Page<RoleEntity> page) {
        List<RoleEntityVo> content = new ArrayList<>();
        page.getContent().forEach(role -> content
                .add(RoleEntityVo.builder()
                        .code(role.getCode())
                        .name(role.getName())
                        .remark(role.getRemark())
                        .status(role.getStatus())
                        .updated(role.getUpdated())
                        .created(role.getCreated())
                        .id(role.getId())
                        .build()));

        return PageAdapter.<RoleEntityVo>builder()
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

    public static List<RoleEntityVo> convert(List<RoleEntity> entities) {
        List<RoleEntityVo> vos = new ArrayList<>();
        entities.forEach(item -> vos
                .add(RoleEntityVo.builder()
                        .code(item.getCode())
                        .id(item.getId())
                        .status(item.getStatus())
                        .name(item.getName())
                        .build()));
        return vos;
    }
}
