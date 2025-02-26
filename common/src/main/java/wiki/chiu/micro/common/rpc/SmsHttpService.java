package wiki.chiu.micro.common.rpc;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;


public interface SmsHttpService {

    @GetExchange("/{paramStr}")
    void sendSms(@PathVariable String paramStr);
}
