package wiki.chiu.micro.auth.adapter.in.security;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.auth.application.model.UserAccount;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;
import wiki.chiu.micro.common.enums.StatusEnum;

@Component
public final class UserDetailsServiceImpl implements UserDetailsService {

    private final UserDirectory users;

    public UserDetailsServiceImpl(UserDirectory users) {
        this.users = users;
    }

    @Override
    @NullUnmarked
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {

        UserAccount user = users.findByLoginName(username);
        List<String> roleCodes = users.findEnabledRoleCodesOf(user.id());

        // 通过User去自动比较用户名和密码
        return new LoginUser(
            username,
            user.password(),
            true,
            true,
            true,
            StatusEnum.NORMAL.getCode().equals(user.status()),
            AuthorityUtils.createAuthorityList(roleCodes),
            user.id());
    }
}
