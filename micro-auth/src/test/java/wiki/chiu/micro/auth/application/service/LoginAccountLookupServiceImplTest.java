package wiki.chiu.micro.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.auth.application.model.LoginAccount;
import wiki.chiu.micro.auth.application.model.UserAccount;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;

class LoginAccountLookupServiceImplTest {

    private final UserDirectory users = mock(UserDirectory.class);
    private final LoginAccountLookupServiceImpl service = new LoginAccountLookupServiceImpl(users);

    @Test
    void loadsTheAccountTogetherWithItsEnabledRoles() {
        when(users.findByLoginName("alice"))
            .thenReturn(new UserAccount(42L, "alice", "hash", "Alice", "avatar", 1));
        when(users.findEnabledRoleCodesOf(42L)).thenReturn(List.of("ADMIN"));

        assertThat(service.byLoginName("alice"))
            .isEqualTo(new LoginAccount(42L, "alice", "hash", 1, List.of("ADMIN")));
    }
}
