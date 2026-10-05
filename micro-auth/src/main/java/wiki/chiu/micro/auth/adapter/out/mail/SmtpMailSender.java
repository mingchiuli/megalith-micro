package wiki.chiu.micro.auth.adapter.out.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.auth.application.port.out.MailSender;

@Component
public class SmtpMailSender implements MailSender {

    private final JavaMailSender javaMailSender;

    private final String from;

    public SmtpMailSender(
        JavaMailSender javaMailSender, @Value("${spring.mail.properties.from}") String from) {
        this.javaMailSender = javaMailSender;
        this.from = from;
    }

    @Override
    public void sendLoginCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Login Code");
        message.setText(code);
        javaMailSender.send(message);
    }
}
