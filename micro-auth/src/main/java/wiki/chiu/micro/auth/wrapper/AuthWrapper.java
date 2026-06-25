package wiki.chiu.micro.auth.wrapper;

import wiki.chiu.micro.auth.convertor.MenuDtoConvertor;
import wiki.chiu.micro.auth.dto.MenuDto;
import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.common.vo.AuthorityRpcVo;
import wiki.chiu.micro.common.vo.MenuRpcVo;
import wiki.chiu.micro.common.lang.Const;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class AuthWrapper {

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    public AuthWrapper(UserHttpServiceWrapper userHttpServiceWrapper) {
        this.userHttpServiceWrapper = userHttpServiceWrapper;
    }

    @Cache(prefix = Const.ROLE_AUTHORITY)
    public List<MenuDto> getCurrentUserNav(String rawRole) {
        List<MenuRpcVo> dto = userHttpServiceWrapper.getCurrentUserNav(rawRole);
        return MenuDtoConvertor.convert(dto);
    }

    @Cache(prefix = Const.ROLE_AUTHORITY)
    public Set<String> getAuthoritiesByRoleCode(String rawRole) {
        return userHttpServiceWrapper.getAuthoritiesByRoleCode(rawRole);
    }

    @Cache(prefix = Const.ALL_SERVICE)
    public List<AuthorityRpcVo> getAllSystemAuthorities() {
        return userHttpServiceWrapper.getSystemAuthorities();
    }

}
