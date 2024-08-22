package org.chiu.micro.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class MenusAndButtonsRpcDto {

    private List<ButtonRpcDto> buttons;

    private List<MenuWithChildRpcDto> menus;
}
