package wiki.chiu.micro.blog.service.port;

public interface CollaborationTicketGateway {

  String issueTicket(Long userId, String roomId);
}
