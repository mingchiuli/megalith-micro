package wiki.chiu.micro.user.adapter.in.http;

import static wiki.chiu.micro.common.constant.Const.*;

import org.springframework.web.servlet.function.ServerRequest;

import wiki.chiu.micro.common.web.ValidatedRequest;

import wiki.chiu.micro.user.application.model.AuthorityDraft;
import wiki.chiu.micro.user.application.model.MenuDraft;
import wiki.chiu.micro.user.application.model.RegistrationDraft;
import wiki.chiu.micro.user.application.model.RoleDraft;
import wiki.chiu.micro.user.application.model.UserDraft;

public final class UserRequestConvertor {

    private static final ValidatedRequest v = new ValidatedRequest();

    private UserRequestConvertor() {
    }

    public static RoleEntityReq toRoleEntityReq(ServerRequest request) throws Exception {
        RoleEntityReq req = request.body(RoleEntityReq.class);

        v.notNull(req.id(), "id");
        req.id().ifPresent(id -> v.positive(id, "id"));
        v.notBlank(req.name(), "name");
        v.notBlank(req.code(), "code");
        v.notNull(req.remark(), "remark");
        v.notNull(req.status(), "status");
        v.range(req.status(), 0, 1, "status");

        return req;
    }

    public static UserEntityReq toUserEntityReq(ServerRequest request) throws Exception {
        UserEntityReq req = request.body(UserEntityReq.class);

        v.notNull(req.id(), "id");
        req.id().ifPresent(id -> v.positive(id, "id"));
        v.notBlank(req.username(), "username");
        if (!req.username().matches(USERNAME_REGEX)) {
            throw new IllegalArgumentException("username format invalid");
        }
        v.notBlank(req.nickname(), "nickname");
        v.notNull(req.avatar(), "avatar");
        if (!req.avatar().matches(URL_REGEX)) {
            throw new IllegalArgumentException("avatar format invalid");
        }
        v.notNull(req.email(), "email");
        if (!req.email().matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("email format invalid");
        }
        v.notNull(req.phone(), "phone");
        if (!req.phone().matches(PHONE_REGEX)) {
            throw new IllegalArgumentException("phone format invalid");
        }
        v.notNull(req.status(), "status");
        v.range(req.status(), 0, 1, "status");
        v.notEmpty(req.roles(), "roles");
        for (String role : req.roles()) {
            v.notBlank(role, "roles element");
        }
        // Cross-field: password required when creating (no id present)
        if (req.id().isEmpty() && (req.password() == null || req.password().isBlank())) {
            throw new IllegalArgumentException("password required when creating user");
        }

        return req;
    }

    public static UserEntityRegisterReq toUserEntityRegisterReq(ServerRequest request)
        throws Exception {
        UserEntityRegisterReq req = request.body(UserEntityRegisterReq.class);

        v.notBlank(req.username(), "username");
        if (!req.username().matches(USERNAME_REGEX)) {
            throw new IllegalArgumentException("username format invalid");
        }
        v.notBlank(req.nickname(), "nickname");
        v.notBlank(req.password(), "password");
        v.notBlank(req.confirmPassword(), "confirmPassword");
        v.notNull(req.email(), "email");
        if (!req.email().matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("email format invalid");
        }
        v.notBlank(req.token(), "token");
        // Cross-field: passwords must match
        if (!req.password().equals(req.confirmPassword())) {
            throw new IllegalArgumentException("passwords do not match");
        }

        return req;
    }

    public static AuthorityEntityReq toAuthorityEntityReq(ServerRequest request) throws Exception {
        AuthorityEntityReq req = request.body(AuthorityEntityReq.class);

        v.notNull(req.id(), "id");
        req.id().ifPresent(id -> v.positive(id, "id"));
        v.notBlank(req.code(), "code");
        v.notBlank(req.remark(), "remark");
        v.notBlank(req.prototype(), "prototype");
        v.notBlank(req.methodType(), "methodType");
        v.notBlank(req.routePattern(), "routePattern");
        v.notBlank(req.serviceHost(), "serviceHost");
        v.notNull(req.servicePort(), "servicePort");
        v.range(req.servicePort(), 1, 65535, "servicePort");
        v.notNull(req.type(), "type");
        v.range(req.type(), 0, 1, "type");
        v.notNull(req.status(), "status");
        v.range(req.status(), 0, 1, "status");

        return req;
    }

    public static MenuEntityReq toMenuEntityReq(ServerRequest request) throws Exception {
        MenuEntityReq req = request.body(MenuEntityReq.class);

        v.notNull(req.id(), "id");
        req.id().ifPresent(id -> v.positive(id, "id"));
        v.notNull(req.parentId(), "parentId");
        v.nonNegative(req.parentId(), "parentId");
        v.notBlank(req.title(), "title");
        v.notBlank(req.name(), "name");
        v.notNull(req.orderNum(), "orderNum");
        v.nonNegative(req.orderNum(), "orderNum");
        v.notNull(req.type(), "type");
        v.range(req.type(), 0, 2, "type");
        v.notNull(req.status(), "status");
        v.range(req.status(), 0, 1, "status");

        return req;
    }
    public static RoleDraft toRoleDraft(ServerRequest request) throws Exception {
        RoleEntityReq req = toRoleEntityReq(request);
        return new RoleDraft(req.id().orElse(null), req.name(), req.code(), req.remark(), req.status());
    }

    public static UserDraft toUserDraft(ServerRequest request) throws Exception {
        UserEntityReq req = toUserEntityReq(request);
        return new UserDraft(
            req.id().orElse(null),
            req.username(),
            req.nickname(),
            req.avatar(),
            req.password(),
            req.email(),
            req.phone(),
            req.status(),
            req.roles());
    }

    public static RegistrationDraft toRegistrationDraft(ServerRequest request) throws Exception {
        UserEntityRegisterReq req = toUserEntityRegisterReq(request);
        return new RegistrationDraft(
            req.username(), req.nickname(), req.avatar(), req.password(), req.email(), req.phone(),
            req.token());
    }

    public static AuthorityDraft toAuthorityDraft(ServerRequest request) throws Exception {
        AuthorityEntityReq req = toAuthorityEntityReq(request);
        return new AuthorityDraft(
            req.id().orElse(null),
            req.code(),
            req.remark(),
            req.prototype(),
            req.methodType(),
            req.routePattern(),
            req.serviceHost(),
            req.servicePort(),
            req.type(),
            req.status());
    }

    public static MenuDraft toMenuDraft(ServerRequest request) throws Exception {
        MenuEntityReq req = toMenuEntityReq(request);
        return new MenuDraft(
            req.id().orElse(null),
            req.parentId(),
            req.title(),
            req.name(),
            req.url(),
            req.component(),
            req.icon(),
            req.orderNum(),
            req.type(),
            req.status());
    }
}
