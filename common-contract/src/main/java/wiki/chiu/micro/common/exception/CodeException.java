package wiki.chiu.micro.common.exception;

import wiki.chiu.micro.common.lang.CommonErrorCode;
import wiki.chiu.micro.common.lang.ErrorCodes;
import wiki.chiu.micro.common.lang.ExceptionMessage;

/**
 * @author mingchiuli
 * @create 2023-04-03 1:48 am
 */
public class CodeException extends BaseException {

  public CodeException(String message) {
    super(ErrorCodes.findByMessage(message).orElse(CommonErrorCode.VALIDATION_ERROR), message);
  }

  public CodeException(ExceptionMessage exceptionMessage) {
    super(exceptionMessage);
  }
}
