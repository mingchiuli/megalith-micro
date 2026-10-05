package wiki.chiu.micro.auth.adapter.out.http;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.auth.application.port.out.SmsSender;
import wiki.chiu.micro.common.rpc.SmsHttpService;

/**
 * Signs and sends login codes through the Aliyun SMS gateway.
 */
@Component
public class AliyunSmsSender implements SmsSender {

    private final SmsHttpService smsHttpService;

    private final JsonMapper jsonMapper;

    private final String accessKeyId;

    private final String accessKeySecret;

    public AliyunSmsSender(
        SmsHttpService smsHttpService,
        JsonMapper jsonMapper,
        @Value("${megalith.blog.aliyun.access-key-id}") String accessKeyId,
        @Value("${megalith.blog.aliyun.access-key-secret}") String accessKeySecret) {
        this.smsHttpService = smsHttpService;
        this.jsonMapper = jsonMapper;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
    }

    @Override
    public void sendLoginCode(String phone, String code) {
        Map<String, Object> codeMap = Collections.singletonMap("code", code);
        String signature =
            AliyunSmsSigner.getSignature(
                phone, jsonMapper.writeValueAsString(codeMap), accessKeyId, accessKeySecret);
        smsHttpService.sendSms("?Signature=" + signature);
    }
}
