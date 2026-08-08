package wiki.chiu.micro.common.req;

public record WebSocketTicketReq(
        Long userId,
        String roomId) {
}
