package org.chiu.micro.user.convertor;

import org.chiu.micro.user.dto.ButtonDto;
import org.chiu.micro.user.entity.MenuEntity;
import org.chiu.micro.user.lang.StatusEnum;

import java.util.List;
import java.util.stream.Stream;

/**
 * @Author limingjiu
 * @Date 2024/4/20 18:40
 **/
public class ButtonDtoConvertor {

    private ButtonDtoConvertor() {
    }

    public static List<ButtonDto> convert(List<ButtonDto> buttons, Boolean statusCheck) {
        Stream<ButtonDto> buttonStream = buttons.stream();
        if (Boolean.TRUE.equals(statusCheck)) {
            buttonStream = buttonStream.filter(menu -> StatusEnum.NORMAL.getCode().equals(menu.getStatus()));
        }

        return buttonStream.toList();
    }

    public static List<ButtonDto> convert(List<MenuEntity> buttons) {
        return buttons.stream()
                .map(item -> ButtonDto.builder()
                        .menuId(item.getMenuId())
                        .name(item.getName())
                        .icon(item.getIcon())
                        .component(item.getComponent())
                        .orderNum(item.getOrderNum())
                        .status(item.getStatus())
                        .parentId(item.getParentId())
                        .url(item.getUrl())
                        .title(item.getTitle())
                        .type(item.getType())
                        .build())
                .toList();
    }
}
