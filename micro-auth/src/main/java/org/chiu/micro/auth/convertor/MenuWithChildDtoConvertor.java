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
						.menuId(menu.menuId())
						.parentId(menu.parentId())
						.icon(menu.icon())
						.url(menu.url())
						.title(menu.title())
						.name(menu.name())
						.component(menu.component())
						.type(menu.type())
						.orderNum(menu.orderNum())
						.status(menu.status())
						.children(convert(menu.children()))
						.build())
				.toList();
	}
}
