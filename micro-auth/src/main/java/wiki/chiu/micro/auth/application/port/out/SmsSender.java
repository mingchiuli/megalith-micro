package wiki.chiu.micro.auth.application.port.out;

public interface SmsSender {

    void sendLoginCode(String phone, String code);
}
