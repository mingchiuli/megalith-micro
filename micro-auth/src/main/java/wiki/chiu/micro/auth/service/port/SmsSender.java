package wiki.chiu.micro.auth.service.port;

public interface SmsSender {

  void send(String signedRequest);
}
