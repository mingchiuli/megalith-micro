package wiki.chiu.micro.auth.rpc;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.auth.service.port.SmsSender;
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
