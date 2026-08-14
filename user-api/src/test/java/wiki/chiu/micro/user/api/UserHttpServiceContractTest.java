package wiki.chiu.micro.user.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;

class UserHttpServiceContractTest {

  @Test
  void passwordLockUsesPatch() throws NoSuchMethodException {
    var method = UserHttpService.class.getMethod("lockAfterPasswordFailures", Long.class);

    assertThat(method.getAnnotation(PatchExchange.class).value())
        .isEqualTo("/user/{userId}/password-lock");
    assertThat(method.getAnnotation(GetExchange.class)).isNull();
  }

  @Test
  void authorityListIsAReadOnlyGet() throws NoSuchMethodException {
    var method = AuthorityHttpService.class.getMethod("getAuthorities");

    assertThat(method.getAnnotation(GetExchange.class).value()).isEqualTo("/authority/list");
  }

  @Test
  void accessAndRoleAuthorizationAreSeparateCacheableResources() throws NoSuchMethodException {
    var method = UserHttpService.class.getMethod("findUserAccess", Long.class);

    assertThat(method.getAnnotation(GetExchange.class).value()).isEqualTo("/user/access/{userId}");
    var roleMethod = UserHttpService.class.getMethod("findRoleAuthorization", Long.class);
    assertThat(roleMethod.getAnnotation(GetExchange.class).value())
        .isEqualTo("/role/authorization/{roleId}");
    var batchRoleMethod =
        UserHttpService.class.getMethod("findRoleAuthorizations", java.util.List.class);
    assertThat(batchRoleMethod.getAnnotation(PostExchange.class).value())
        .isEqualTo("/role/authorizations");
  }
}
