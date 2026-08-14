package wiki.chiu.micro.search.convertor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PrivateSearchQueryConvertorTest {

  @Test
  void ownDataSearchAddsUserIdFilter() {
    String query =
        PrivateSearchQueryConvertor.countConvert(null, null, null, null, 42L, false)
            .getQuery()
            .toString();

    assertTrue(query.contains("userId"));
    assertTrue(query.contains("42"));
  }

  @Test
  void allDataSearchDoesNotAddUserIdFilter() {
    String query =
        PrivateSearchQueryConvertor.countConvert(null, null, null, null, 42L, true)
            .getQuery()
            .toString();

    assertFalse(query.contains("userId"));
  }
}
