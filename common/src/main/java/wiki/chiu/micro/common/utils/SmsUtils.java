package wiki.chiu.micro.common.utils;

import wiki.chiu.micro.common.exception.MissException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class SmsUtils {

    @Value("${megalith.blog.aliyun.access-key-id}")
    private String accessKeyId;

    @Value("${megalith.blog.aliyun.access-key-secret}")
    private String accessKeySecret;

    private static final String ALGORITHM = "HmacSHA1";

    private static final String SIGNATURE_METHOD = "HMAC-SHA1";


    private String sign(String accessSecret, String stringToSign) {
        Mac mac;
        try {
            mac = Mac.getInstance(ALGORITHM);
            mac.init(new javax.crypto.spec.SecretKeySpec(accessSecret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new MissException(e.getMessage());
        }

        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signData);
    }

    private String specialUrlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

    public String getSignature(String phoneNumbers, String templateParam) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("GMT"));
        String timeStamp = dateTimeFormatter.format(now);

        Map<String, String> paras = new HashMap<>();
        // 1. 系统参数
        paras.put("SignatureMethod", SIGNATURE_METHOD);
        paras.put("SignatureNonce", UUID.randomUUID().toString());
        paras.put("AccessKeyId", accessKeyId);
        paras.put("SignatureVersion", "1.0");
        paras.put("Timestamp", timeStamp);
        paras.put("Format", "XML");
        // 2. 业务API参数
        paras.put("Action", "SendSms");
        paras.put("Version", "2017-05-25");
        paras.put("RegionId", "cn-hangzhou");
        paras.put("PhoneNumbers", phoneNumbers);
        paras.put("SignName", "阿里云短信测试");
        paras.put("TemplateParam", templateParam);
        paras.put("TemplateCode", "SMS_154950909");
        // 4. 参数KEY排序
        TreeMap<String, String> sortParas = new TreeMap<>(paras);
        // 5. 构造待签名的字符串
        Iterator<String> it = sortParas.keySet().iterator();
        StringBuilder sortQueryStringTmp = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            sortQueryStringTmp
                    .append("&")
                    .append(specialUrlEncode(key))
                    .append("=")
                    .append(specialUrlEncode(paras.get(key)));
        }
        String sortedQueryString = sortQueryStringTmp.substring(1);// 去除第一个多余的&符号
        String stringToSign = "GET" + "&" +
                specialUrlEncode("/") + "&" +
                specialUrlEncode(sortedQueryString);
        String sign = sign(accessKeySecret + "&", stringToSign);
        // 6. 签名最后也要做特殊URL编码
        String signature = specialUrlEncode(sign);
        String string = sortQueryStringTmp.toString();
        return signature + string;
    }
}
