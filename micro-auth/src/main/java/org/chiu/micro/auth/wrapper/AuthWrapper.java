package org.chiu.micro.auth.wrapper;

import org.chiu.micro.auth.cache.Cache;
import org.chiu.micro.auth.convertor.MenusAndButtonsDtoConvertor;
import org.chiu.micro.auth.dto.AuthorityDto;
import org.chiu.micro.auth.dto.MenusAndButtonsDto;
import org.chiu.micro.auth.dto.MenusAndButtonsRpcDto;
import org.chiu.micro.auth.lang.Const;
import org.chiu.micro.auth.rpc.wrapper.UserHttpServiceWrapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthWrapper {

    private final List<String> services = List.of(Const.AUTH_SERVICE.getInfo(), Const.BLOG_SERVICE.getInfo(), Const.EXHIBIT_SERVICE.getInfo(), Const.USER_SERVICE.getInfo(), Const.SEARCH_SERVICE.getInfo(), Const.WEBSOCKET_SERVICE.getInfo());

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    public AuthWrapper(UserHttpServiceWrapper userHttpServiceWrapper) {
        this.userHttpServiceWrapper = userHttpServiceWrapper;
    }

    @Cache(prefix = Const.HOT_MENUS_AND_BUTTONS)
    public MenusAndButtonsDto getCurrentUserNav(String rawRole) {
        MenusAndButtonsRpcDto dto = userHttpServiceWrapper.getCurrentUserNav(rawRole);
        return MenusAndButtonsDtoConvertor.convert(dto);
    }

    @Cache(prefix = Const.HOT_AUTHORITIES)
    public List<String> getAuthoritiesByRoleCode(String rawRole) {
        return userHttpServiceWrapper.getAuthoritiesByRoleCode(rawRole);
    }

    @Cache(prefix = Const.HOT_AUTHORITIES)
    public List<AuthorityDto> getAllSystemAuthorities() {
        return userHttpServiceWrapper.getSystemAuthorities(services);
    }

}
