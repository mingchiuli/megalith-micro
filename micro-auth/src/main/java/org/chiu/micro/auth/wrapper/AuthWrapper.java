package org.chiu.micro.auth.wrapper;

import java.util.List;

import org.chiu.micro.auth.cache.Cache;
import org.chiu.micro.auth.convertor.MenusAndButtonsDtoConvertor;
import org.chiu.micro.auth.dto.MenusAndButtonsDto;
import org.chiu.micro.auth.dto.MenusAndButtonsRpcDto;
import org.chiu.micro.auth.lang.Const;
import org.chiu.micro.auth.rpc.wrapper.UserHttpServiceWrapper;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthWrapper {

	private final UserHttpServiceWrapper userHttpServiceWrapper;

	@Cache(prefix = Const.HOT_MENUS_AND_BUTTONS)
	public MenusAndButtonsDto getCurrentUserNav(String rawRole) {
		MenusAndButtonsRpcDto dto = userHttpServiceWrapper.getCurrentUserNav(rawRole);
		return MenusAndButtonsDtoConvertor.convert(dto);
	}

	@Cache(prefix = Const.HOT_AUTHORITIES)
	public List<String> getAuthoritiesByRoleCode(String rawRole) {
		return userHttpServiceWrapper.getAuthoritiesByRoleCode(rawRole);
	}

}
