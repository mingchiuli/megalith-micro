package wiki.chiu.micro.blog.adapter.out.http.auth;



public record WebSocketTicketReq(String roomId, Long expiresAt) {
}
