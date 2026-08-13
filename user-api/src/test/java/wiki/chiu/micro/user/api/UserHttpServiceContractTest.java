package wiki.chiu.micro.user.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PatchExchange;

class UserHttpServiceContractTest {

  @Test
  void statusMutationUsesPatch() throws NoSuchMethodException {
    var method =
        UserHttpService.class.getMethod("changeUserStatusByUsername", String.class, Integer.class);

    assertThat(method.getAnnotation(PatchExchange.class).value()).isEqualTo("/user/status");
    assertThat(method.getAnnotation(GetExchange.class)).isNull();
  }

  @Test
  void authorityListIsAReadOnlyGet() throws NoSuchMethodException {
    var method = AuthorityHttpService.class.getMethod("getAuthorities");

    assertThat(method.getAnnotation(GetExchange.class).value()).isEqualTo("/authority/list");
  }

  @Test
  void authContextReplacesTheSeparateRoleLookup() throws NoSuchMethodException {
    var method = UserHttpService.class.getMethod("findAuthContext", Long.class);

    assertThat(method.getAnnotation(GetExchange.class).value())
        .isEqualTo("/user/auth-context/{userId}");
    assertThat(
            java.util.Arrays.stream(UserHttpService.class.getMethods())
                .map(java.lang.reflect.Method::getName))
        .doesNotContain("findRoleCodesByUserId");
  }
}
