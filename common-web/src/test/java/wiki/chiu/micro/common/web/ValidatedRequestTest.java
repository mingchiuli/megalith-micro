package wiki.chiu.micro.common.web;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ValidatedRequestTest {

  private final ValidatedRequest validation = new ValidatedRequest();
  private final LocalDateTime point = LocalDateTime.of(2026, 8, 8, 12, 0);

  @Test
  void dateRangeAcceptsOpenAndEqualEndpoints() {
    assertDoesNotThrow(() -> validation.dateRange(null, point, "start", "end"));
    assertDoesNotThrow(() -> validation.dateRange(point, null, "start", "end"));
    assertDoesNotThrow(() -> validation.dateRange(point, point, "start", "end"));
  }

  @Test
  void dateRangeRejectsOnlyReversedEndpoints() {
    assertThrows(
        IllegalArgumentException.class,
        () -> validation.dateRange(point.plusSeconds(1), point, "start", "end"));
  }
}
