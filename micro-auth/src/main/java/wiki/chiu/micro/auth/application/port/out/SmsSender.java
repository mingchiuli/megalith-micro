package wiki.chiu.micro.auth.application.port.out;

public interface SmsSender {

    void send(String signedRequest);
}
