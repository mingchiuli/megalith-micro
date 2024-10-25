package org.chiu.micro.auth.wrapper;

import org.chiu.micro.auth.convertor.MenusAndButtonsDtoConvertor;
import org.chiu.micro.auth.dto.MenusAndButtonsDto;
import org.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import org.chiu.micro.common.dto.AuthorityRpcDto;
import org.chiu.micro.common.dto.MenusAndButtonsRpcDto;
import org.chiu.micro.common.lang.Const;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthWrapper {

    private final List<String> services = List.of(Const.AUTH_SERVICE, Const.BLOG_SERVICE, Const.EXHIBIT_SERVICE, Const.USER_SERVICE, Const.SEARCH_SERVICE, Const.WEBSOCKET_SERVICE);

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    public AuthWrapper(UserHttpServiceWrapper userHttpServiceWrapper) {
        this.userHttpServiceWrapper = userHttpServiceWrapper;
    }

    public MenusAndButtonsDto getCurrentUserNav(String rawRole) {
        MenusAndButtonsRpcDto dto = userHttpServiceWrapper.getCurrentUserNav(rawRole);
        return MenusAndButtonsDtoConvertor.convert(dto);
    }

    public List<String> getAuthoritiesByRoleCode(String rawRole) {
        return userHttpServiceWrapper.getAuthoritiesByRoleCode(rawRole);
    }

    public List<AuthorityRpcDto> getAllSystemAuthorities() {
        return userHttpServiceWrapper.getSystemAuthorities(services);
    }

}
