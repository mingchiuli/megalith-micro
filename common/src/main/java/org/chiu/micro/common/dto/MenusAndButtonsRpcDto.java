package org.chiu.micro.common.dto;

import java.util.List;

public record MenusAndButtonsRpcDto (

        List<ButtonRpcDto> buttons,

        List<MenuRpcDto> menus) {
}
