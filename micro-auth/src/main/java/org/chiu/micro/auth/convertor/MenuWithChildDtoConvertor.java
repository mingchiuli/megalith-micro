package org.chiu.micro.auth.convertor;

import java.util.List;

import org.chiu.micro.auth.dto.MenuWithChildDto;
import org.chiu.micro.auth.dto.MenuWithChildRpcDto;

public class MenuWithChildDtoConvertor {

	private MenuWithChildDtoConvertor() {
	}

	public static List<MenuWithChildDto> convert(List<MenuWithChildRpcDto> menuVos) {
		return menuVos.stream()
				.map(menu -> MenuWithChildDto.builder()
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
						.children(convert(menu.getChildren()))
						.build())
				.toList();
	}
}
