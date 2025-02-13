package wiki.chiu.micro.auth.wrapper;

import wiki.chiu.micro.auth.convertor.MenusAndButtonsDtoConvertor;
import wiki.chiu.micro.auth.dto.MenusAndButtonsDto;
import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.common.vo.AuthorityRpcVo;
import wiki.chiu.micro.common.vo.MenusAndButtonsRpcVo;
import wiki.chiu.micro.common.lang.Const;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.lang.ServiceHostEnum;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
public class AuthWrapper {

    private final List<String> services = Arrays.stream(ServiceHostEnum.values())
            .map(ServiceHostEnum::getServiceHost)
            .toList();

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    public AuthWrapper(UserHttpServiceWrapper userHttpServiceWrapper) {
        this.userHttpServiceWrapper = userHttpServiceWrapper;
    }

    @Cache(prefix = Const.ROLE_AUTHORITY)
    public MenusAndButtonsDto getCurrentUserNav(String rawRole) {
        MenusAndButtonsRpcVo dto = userHttpServiceWrapper.getCurrentUserNav(rawRole);
        return MenusAndButtonsDtoConvertor.convert(dto);
    }

    @Cache(prefix = Const.ROLE_AUTHORITY)
    public Set<String> getAuthoritiesByRoleCode(String rawRole) {
        return userHttpServiceWrapper.getAuthoritiesByRoleCode(rawRole);
    }

    @Cache(prefix = Const.ALL_SERVICE)
    public List<AuthorityRpcVo> getAllSystemAuthorities() {
        return userHttpServiceWrapper.getSystemAuthorities(services);
    }

}
