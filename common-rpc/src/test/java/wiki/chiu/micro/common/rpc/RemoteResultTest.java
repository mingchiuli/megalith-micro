package wiki.chiu.micro.common.rpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import wiki.chiu.micro.common.exception.RemoteServiceException;
import wiki.chiu.micro.common.lang.CommonErrorCode;
import wiki.chiu.micro.common.lang.Result;

class RemoteResultTest {

  @Test
  void returnsDataFromSuccessfulResult() {
    assertThat(RemoteResult.requireSuccess(() -> Result.success("value"))).isEqualTo("value");
  }

  @Test
  void rejectsFailedResult() {
    assertThatThrownBy(
            () ->
                RemoteResult.requireSuccess(
                    () -> Result.fail(CommonErrorCode.VALIDATION_ERROR, "invalid")))
        .isInstanceOf(RemoteServiceException.class)
        .hasMessage("invalid");
  }

  @Test
  void rejectsEmptyResponse() {
    assertThatThrownBy(() -> RemoteResult.requireSuccess(() -> null))
        .isInstanceOf(RemoteServiceException.class)
        .hasMessage("downstream service returned an empty response");
  }
}
