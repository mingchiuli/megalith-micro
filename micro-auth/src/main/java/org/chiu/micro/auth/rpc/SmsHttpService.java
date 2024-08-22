package org.chiu.micro.auth.rpc;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;


public interface SmsHttpService {

    @GetExchange("/{paramStr}")
    void sendSms(@PathVariable String paramStr);
}
