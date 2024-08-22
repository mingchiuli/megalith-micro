package org.chiu.micro.user.convertor;

import java.util.List;

import org.chiu.micro.user.dto.MenuWithChildDto;
import org.chiu.micro.user.vo.MenuWithChildVo;

public class MenuWithChildVoConvertor {

	private MenuWithChildVoConvertor() {
	}

	public static List<MenuWithChildVo> convert(List<MenuWithChildDto> menuVos) {
		return menuVos.stream()
				.map(menu -> MenuWithChildVo.builder()
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
