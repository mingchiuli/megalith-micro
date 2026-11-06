package wiki.chiu.micro.blog.application.model;



public record CollaborationTicket(String ticket, String roomId, String collaborationEpoch, long expiresAt) {
}
