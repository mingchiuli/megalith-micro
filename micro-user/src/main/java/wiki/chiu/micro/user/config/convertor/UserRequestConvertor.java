package wiki.chiu.micro.user.config.convertor;

import static wiki.chiu.micro.common.lang.Const.*;

import org.springframework.web.servlet.function.ServerRequest;
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.user.req.*;

public final class UserRequestConvertor {

  private static final ValidatedRequest v = new ValidatedRequest();

  private UserRequestConvertor() {}

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
}
