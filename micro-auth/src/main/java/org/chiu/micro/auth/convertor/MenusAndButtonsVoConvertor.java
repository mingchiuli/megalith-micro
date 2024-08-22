package org.chiu.micro.auth.convertor;

import org.chiu.micro.auth.dto.ButtonDto;
import org.chiu.micro.auth.dto.MenuWithChildDto;
import org.chiu.micro.auth.dto.MenusAndButtonsDto;
import org.chiu.micro.auth.vo.ButtonVo;
import org.chiu.micro.auth.vo.MenuWithChildVo;
import org.chiu.micro.auth.vo.MenusAndButtonsVo;

import java.util.List;
import java.util.ArrayList;

public class MenusAndButtonsVoConvertor {

	private MenusAndButtonsVoConvertor() {}

	public static MenusAndButtonsVo convert(MenusAndButtonsDto dto) {
		List<ButtonDto> buttons = dto.getButtons();
		List<MenuWithChildDto> menus = dto.getMenus();

		buttons = buttons.stream()
				.distinct()
				.toList();

		menus = menus.stream()
				.distinct()
				.toList();

		List<ButtonVo> buttonVos = new ArrayList<>();
		List<MenuWithChildVo> menuVos = new ArrayList<>();

		buttons.forEach(button -> buttonVos.add(ButtonVo.builder()
				.menuId(button.getMenuId())
				.parentId(button.getParentId())
				.icon(button.getIcon())
				.url(button.getUrl())
				.title(button.getTitle())
				.name(button.getName())
				.component(button.getComponent())
				.type(button.getType())
				.orderNum(button.getOrderNum())
				.status(button.getStatus())
				.build()));

		menus.forEach(menu -> menuVos.add(MenuWithChildVo.builder()
				.menuId(menu.getMenuId())
				.parentId(menu.getParentId())
				.icon(menu.getIcon())
				.url(menu.getUrl())
				.title(menu.getTitle())
				.name(menu.getName())
				.component(menu.getComponent())
				.type(menu.getType())
				.orderNum(menu.getOrderNum())
				.status(menu.getStatus())
				.children(MenuWithChildVoConvertor.convert(menu.getChildren()))
				.build()));

		return MenusAndButtonsVo.builder()
				.buttons(buttonVos)
				.menus(menuVos)
				.build();
	}

}
