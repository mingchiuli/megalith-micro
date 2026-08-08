package wiki.chiu.micro.common.lang;

import java.util.Arrays;
import java.util.Optional;

public final class ErrorCodes {

  private ErrorCodes() {}

  public static Optional<ErrorCode> find(int code) {
    return Arrays.stream(ExceptionMessage.values())
        .filter(value -> value.code() == code)
        .map(ErrorCode.class::cast)
        .findFirst()
        .or(
            () ->
                Arrays.stream(CommonErrorCode.values())
                    .filter(value -> value.code() == code)
                    .map(ErrorCode.class::cast)
                    .findFirst());
  }

  public static Optional<ErrorCode> findByMessage(String message) {
    if (message == null) {
      return Optional.empty();
    }
    return Arrays.stream(ExceptionMessage.values())
        .filter(value -> value.defaultMessage().equals(message))
        .map(ErrorCode.class::cast)
        .findFirst();
  }
}
