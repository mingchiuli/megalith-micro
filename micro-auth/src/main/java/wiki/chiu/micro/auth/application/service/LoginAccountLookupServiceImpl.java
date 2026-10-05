package wiki.chiu.micro.auth.application.service;

import java.util.List;

import wiki.chiu.micro.auth.application.model.LoginAccount;
import wiki.chiu.micro.auth.application.model.UserAccount;
import wiki.chiu.micro.auth.application.port.in.LoginAccountLookup;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;

public class LoginAccountLookupServiceImpl implements LoginAccountLookup {

    private final UserDirectory users;

    public LoginAccountLookupServiceImpl(UserDirectory users) {
        this.users = users;
    }

    @Override
    public LoginAccount byLoginName(String loginName) {
        UserAccount account = users.findByLoginName(loginName);
        List<String> roles = users.findEnabledRoleCodesOf(account.id());
        return new LoginAccount(
            account.id(), account.username(), account.password(), account.status(), roles);
    }
}
