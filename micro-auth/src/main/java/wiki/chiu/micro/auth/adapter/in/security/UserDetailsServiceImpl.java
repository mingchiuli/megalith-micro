package wiki.chiu.micro.auth.adapter.in.security;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.auth.application.model.LoginAccount;
import wiki.chiu.micro.auth.application.port.in.LoginAccountLookup;
import wiki.chiu.micro.common.enums.StatusEnum;

@Component
public final class UserDetailsServiceImpl implements UserDetailsService {

    private final LoginAccountLookup accounts;

    public UserDetailsServiceImpl(LoginAccountLookup accounts) {
        this.accounts = accounts;
    }

    @Override
    @NullUnmarked
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {

        LoginAccount account = accounts.byLoginName(username);

        // 通过User去自动比较用户名和密码
        return new LoginUser(
            username,
            account.password(),
            true,
            true,
            true,
            StatusEnum.NORMAL.getCode().equals(account.status()),
            AuthorityUtils.createAuthorityList(account.roleCodes()),
            account.userId());
    }
}
