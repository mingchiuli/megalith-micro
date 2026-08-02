package wiki.chiu.micro.common.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record WebSocketTicketReq(
        @Positive Long userId,
        @NotBlank String roomId) {
}
