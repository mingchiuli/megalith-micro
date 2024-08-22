package org.chiu.micro.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MenusWithChildAndButtonsDto {
 
  private List<MenuWithChildDto> menus;

  private List<ButtonDto> buttons;
}
