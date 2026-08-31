package wiki.chiu.micro.common.auth.web;

import java.util.Base64;
import java.util.List;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.common.exception.ValidationException;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.common.security.AuthPrincipal;
import wiki.chiu.micro.common.security.InternalHttpHeaders;

public final class AuthPrincipalCodec {

    public static final String HEADER_NAME = InternalHttpHeaders.PRINCIPAL;
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private AuthPrincipalCodec() {
    }

    public static AuthPrincipal decode(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return AuthPrincipal.anonymous();
        }
        try {
            byte[] json = Base64.getUrlDecoder().decode(encoded);
            return validate(JSON.readValue(json, AuthPrincipal.class));
        } catch (Exception exception) {
            throw new ValidationException("invalid internal principal");
        }
    }

    public static AuthPrincipal decodeRequired(String encoded) {
        AuthPrincipal principal = decode(encoded);
        if (principal.userId() == 0L) {
            throw new ValidationException("authenticated internal principal is required");
        }
        return principal;
    }

    public static String encode(AuthPrincipal principal) {
        try {
            return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(JSON.writeValueAsBytes(validate(principal)));
        } catch (Exception exception) {
            throw new IllegalArgumentException("failed to encode internal principal", exception);
        }
    }

    private static AuthPrincipal validate(AuthPrincipal principal) {
        if (principal == null || principal.userId() == null || principal.userId() < 0) {
            throw new ValidationException("invalid internal principal userId");
        }
        List<String> roles = principal.roles();
        if (roles == null || roles.stream().anyMatch(role -> role == null || role.isBlank())) {
            throw new ValidationException("invalid internal principal roles");
        }
        if (principal.userId() == 0L && !roles.isEmpty()) {
            throw new ValidationException("anonymous internal principal cannot have roles");
        }
        List<DataPermissionEnum> dataPermissions =
            principal.dataPermissions() == null ? List.of() : principal.dataPermissions();
        if (dataPermissions.stream().anyMatch(java.util.Objects::isNull)) {
            throw new ValidationException("invalid internal principal dataPermissions");
        }
        if (principal.userId() == 0L && !dataPermissions.isEmpty()) {
            throw new ValidationException("anonymous internal principal cannot have data permissions");
        }
        return new AuthPrincipal(principal.userId(), List.copyOf(roles), List.copyOf(dataPermissions));
    }
}
