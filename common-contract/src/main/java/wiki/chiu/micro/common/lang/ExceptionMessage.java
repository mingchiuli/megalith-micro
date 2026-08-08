package wiki.chiu.micro.common.lang;

public enum ExceptionMessage implements ErrorCode {
  NO_FOUND(1, "没有找到内容", ErrorCategory.NOT_FOUND),

  TOKEN_INVALID(8, "token非法", ErrorCategory.UNAUTHORIZED),

  AUTH_EXCEPTION(10, "认证异常", ErrorCategory.UNAUTHORIZED),

  CODE_TRY_MAX(19, "code reach max try number", ErrorCategory.VALIDATION),

  CODE_EXPIRED(20, "code expired", ErrorCategory.VALIDATION),

  CODE_MISMATCH(21, "code mismatch", ErrorCategory.VALIDATION),

  CODE_NOT_EXIST(22, "code not exist", ErrorCategory.NOT_FOUND),

  CODE_EXISTED(23, "code existed", ErrorCategory.CONFLICT),

  PASSWORD_MISMATCH(
      24,
      "Failed to authenticate since password does not match stored value",
      ErrorCategory.UNAUTHORIZED),

  PASSWORD_MISS(
      25, "Failed to authenticate since no credentials provided", ErrorCategory.UNAUTHORIZED),

  SMS_TRY_MAX(26, "sms reach max try number", ErrorCategory.VALIDATION),

  SMS_EXPIRED(27, "sms expired", ErrorCategory.VALIDATION),

  SMS_MISMATCH(28, "sms mismatch", ErrorCategory.VALIDATION),

  SMS_NOT_EXIST(29, "sms not exist", ErrorCategory.NOT_FOUND),

  ROLE_DISABLED(31, "role disabled", ErrorCategory.FORBIDDEN),

  INVALID_LOGIN_OPERATE(33, "非法登录", ErrorCategory.UNAUTHORIZED),

  ACCOUNT_LOCKED(34, "账户被锁", ErrorCategory.FORBIDDEN),

  UPLOAD_MISS(36, "上传出现错误", ErrorCategory.UPSTREAM),

  USER_MISS(37, "用户没有找到", ErrorCategory.NOT_FOUND),

  EDIT_NO_AUTH(38, "必须编辑自己的文章", ErrorCategory.FORBIDDEN),

  MENU_NOT_EXIST(39, "menu不存在", ErrorCategory.NOT_FOUND),

  ROLE_NOT_EXIST(40, "role不存在", ErrorCategory.NOT_FOUND),

  USER_NOT_EXIST(41, "user不存在", ErrorCategory.NOT_FOUND),

  PASSWORD_REQUIRED(42, "需要密码", ErrorCategory.VALIDATION),

  EMAIL_NOT_EXIST(43, "email不存在", ErrorCategory.NOT_FOUND),

  PHONE_NOT_EXIST(44, "phone not exist", ErrorCategory.NOT_FOUND),

  MENU_INVALID_OPERATE(45, "先删除子菜单，不允许直接删除父菜单", ErrorCategory.CONFLICT),

  PASSWORD_DIFF(46, "账号密码不一致", ErrorCategory.UNAUTHORIZED),

  BUTTON_MUST_NOT_PARENT(47, "按钮不能有子元素", ErrorCategory.CONFLICT),

  MENU_CHILDREN_MUST_BE_BUTTON(48, "菜单子元素只能为按钮", ErrorCategory.CONFLICT),

  CATALOGUE_CHILD_MUST_NOT_BUTTON(49, "分类的子元素不能是按钮", ErrorCategory.CONFLICT),

  CATALOGUE_PARENT_MUST_PARENT(50, "分类的父元素只能是分类", ErrorCategory.CONFLICT),

  NO_AUTH(51, "没有权限", ErrorCategory.FORBIDDEN);

  private final Integer code;

  private final String msg;
  private final ErrorCategory category;

  ExceptionMessage(Integer code, String msg, ErrorCategory category) {
    this.code = code;
    this.msg = msg;
    this.category = category;
  }

  public Integer getCode() {
    return this.code;
  }

  public String getMsg() {
    return this.msg;
  }

  @Override
  public int code() {
    return code;
  }

  @Override
  public String defaultMessage() {
    return msg;
  }

  @Override
  public ErrorCategory category() {
    return category;
  }
}
