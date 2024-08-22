package org.chiu.micro.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenusAndButtonsDto implements Serializable {

    private List<ButtonDto> buttons;

    private List<MenuWithChildDto> menus;
}
