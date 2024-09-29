package org.chiu.micro.auth.dto;

import java.util.List;

public record MenusAndButtonsRpcDto (

        List<ButtonRpcDto> buttons,

        List<MenuRpcDto> menus) {
}
