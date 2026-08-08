package wiki.chiu.micro.common.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import wiki.chiu.micro.common.validation.DateRangeRequest;

class ErrorCodeTest {

  @Test
  void allBusinessErrorCodesAreUniqueAndResolvable() {
    var codes = new HashSet<Integer>();
    Stream.concat(
            Stream.of(ExceptionMessage.values()).map(ErrorCode.class::cast),
            Stream.of(CommonErrorCode.values()).map(ErrorCode.class::cast))
        .forEach(
            errorCode -> {
              assertTrue(codes.add(errorCode.code()), "duplicate code " + errorCode.code());
              assertEquals(errorCode, ErrorCodes.find(errorCode.code()).orElseThrow());
            });
  }

  @Test
  void successAndFailureKeepTheStableWireShape() {
    Result<String> success = Result.success("value");
    Result<Void> failure = Result.fail(CommonErrorCode.VALIDATION_ERROR, "invalid");

    assertEquals("success", success.msg());
    assertEquals(200, success.code());
    assertEquals("value", success.data());
    assertEquals("invalid", failure.msg());
    assertEquals(1000, failure.code());
  }

  @Test
  void dateRangeContractAcceptsOpenAndEqualEndpoints() {
    var point = java.time.LocalDateTime.of(2026, 8, 8, 12, 0);

    assertTrue(new Range(null, point).hasValidDateRange());
    assertTrue(new Range(point, null).hasValidDateRange());
    assertTrue(new Range(point, point).hasValidDateRange());
    assertFalse(new Range(point.plusSeconds(1), point).hasValidDateRange());
  }

  private record Range(java.time.LocalDateTime createStart, java.time.LocalDateTime createEnd)
      implements DateRangeRequest {}
}
