package wiki.chiu.micro.common.exception;

import wiki.chiu.micro.common.lang.ExceptionMessage;

public class AuthException extends BaseException {

  public AuthException(String string) {
    super(ExceptionMessage.NO_AUTH, string);
  }
}
