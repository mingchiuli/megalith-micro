package wiki.chiu.micro.auth.application.port.out;

public interface MailSender {

    void sendLoginCode(String to, String code);
}
