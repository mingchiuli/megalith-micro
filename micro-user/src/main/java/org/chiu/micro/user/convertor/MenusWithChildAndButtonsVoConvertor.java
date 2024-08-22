package org.chiu.micro.user.convertor;

import java.util.List;
import java.util.ArrayList;

import org.chiu.micro.user.dto.ButtonDto;
import org.chiu.micro.user.dto.MenuWithChildDto;
import org.chiu.micro.user.dto.MenusWithChildAndButtonsDto;
import org.chiu.micro.user.vo.ButtonVo;
import org.chiu.micro.user.vo.MenuWithChildVo;
import org.chiu.micro.user.vo.MenusAndButtonsVo;

public class MenusWithChildAndButtonsVoConvertor {

	private MenusWithChildAndButtonsVoConvertor() {
	}

	public static MenusAndButtonsVo convert(MenusWithChildAndButtonsDto dto) {
		List<ButtonDto> buttons = dto.getButtons();
		List<MenuWithChildDto> menus = dto.getMenus();

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
