package org.chiu.micro.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;


/**
 * @Author limingjiu
 * @Date 2024/4/20 18:17
 **/
@Data
@Builder
public class MenusAndButtonsDto {

    private List<MenuDto> menus;

    private List<ButtonDto> buttons;

}
