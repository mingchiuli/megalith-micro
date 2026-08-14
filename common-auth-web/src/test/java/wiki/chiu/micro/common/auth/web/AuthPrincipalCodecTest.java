package wiki.chiu.micro.common.auth.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;
import wiki.chiu.micro.common.exception.ValidationException;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.common.security.AuthPrincipal;

class AuthPrincipalCodecTest {

  @Test
  void roundTripsAuthenticatedPrincipal() {
    AuthPrincipal principal =
        new AuthPrincipal(
            42L, List.of("user", "editor"), List.of(DataPermissionEnum.BLOG_VIEW_ALL));

    assertEquals(principal, AuthPrincipalCodec.decode(AuthPrincipalCodec.encode(principal)));
  }

  @Test
  void missingHeaderIsAnonymous() {
    assertEquals(AuthPrincipal.anonymous(), AuthPrincipalCodec.decode(null));
  }

  @Test
  void oldPrincipalWithoutDataPermissionsFallsBackToOwnData() {
    String encoded =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(
                "{\"userId\":42,\"roles\":[\"user\"]}".getBytes(StandardCharsets.UTF_8));

    assertEquals(List.of(), AuthPrincipalCodec.decode(encoded).dataPermissions());
  }

  @Test
  void rejectsMalformedAndInvalidPrincipals() {
    assertThrows(ValidationException.class, () -> AuthPrincipalCodec.decode("not-base64"));
    String invalid =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(
                "{\"userId\":0,\"roles\":[\"admin\"]}".getBytes(StandardCharsets.UTF_8));
    assertThrows(ValidationException.class, () -> AuthPrincipalCodec.decode(invalid));
  }

  @Test
  void requiredPrincipalRejectsAnonymous() {
    assertThrows(ValidationException.class, () -> AuthPrincipalCodec.decodeRequired(null));
  }
}
