package wiki.chiu.micro.auth.user;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.auth.adapter.out.http.UserHttpServiceWrapper;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

@Component
public final class UserDetailsServiceImpl implements UserDetailsService {

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    public UserDetailsServiceImpl(UserHttpServiceWrapper userHttpServiceWrapper) {
        this.userHttpServiceWrapper = userHttpServiceWrapper;
    }

    @Override
    @NullUnmarked
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {

        UserEntityRpcVo user = userHttpServiceWrapper.findByUsernameOrEmailOrPhone(username);

        Long userId = user.id();
        List<Long> roleIds = userHttpServiceWrapper.findUserAccess(userId).roleIds();
        List<String> roleCodes =
            userHttpServiceWrapper.findRoleAuthorizations(roleIds).stream()
                .filter(RoleAuthorizationRpcVo::exists)
                .filter(role -> StatusEnum.NORMAL.getCode().equals(role.status()))
                .map(RoleAuthorizationRpcVo::code)
                .toList();

        // 通过User去自动比较用户名和密码
        return new LoginUser(
            username,
            user.password(),
            true,
            true,
            true,
            StatusEnum.NORMAL.getCode().equals(user.status()),
            AuthorityUtils.createAuthorityList(roleCodes),
            userId);
    }
}
