package wiki.chiu.micro.auth.adapter.out.http;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.auth.application.port.out.SmsSender;
import wiki.chiu.micro.common.rpc.SmsHttpService;

@Component
public class SmsHttpServiceAdapter implements SmsSender {

  private final SmsHttpService smsHttpService;

  public SmsHttpServiceAdapter(SmsHttpService smsHttpService) {
    this.smsHttpService = smsHttpService;
  }

  @Override
  public void send(String signedRequest) {
    smsHttpService.sendSms(signedRequest);
  }
}
