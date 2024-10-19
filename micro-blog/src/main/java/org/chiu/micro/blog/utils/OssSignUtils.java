package org.chiu.micro.blog.utils;

import org.chiu.micro.common.exception.MissException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

@Component
public class OssSignUtils {

    @Value("${megalith.blog.aliyun.access-key-id}")
    private String accessKeyId;

    @Value("${megalith.blog.aliyun.access-key-secret}")
    private String accessKeySecret;

    @Value("${megalith.blog.aliyun.oss.bucket-name}")
    private String bucketName;

    private static final String ALGORITHM = "HmacSHA1";

    private byte[] hmacSha1(String data, String accessKeySecret) {
        Mac mac;
        try {
            mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(accessKeySecret.getBytes(), ALGORITHM);
            mac.init(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new MissException(e.getMessage());
        }
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    public String getGMTDate() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.UK);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("GMT"));
        return dateTimeFormatter.format(now);
    }

    private String buildSignData(String date, String canonicalizedResource, String methodName, String contentType) {
        //https://help.aliyun.com/zh/oss/developer-reference/include-signatures-in-the-authorization-header?spm=a2c4g.11186623.0.0.54e828efd3PoE6
        return methodName + "\n" +
                "\n" +
                contentType + "\n" +
                date + "\n" +
                canonicalizedResource;
    }

    public String getAuthorization(String objectName, String method, String contentType) {
        String date = getGMTDate();
        String signData = buildSignData(date, "/" + bucketName + "/" + objectName, method, contentType);
        byte[] bytes = hmacSha1(signData, accessKeySecret);
        String signature = Base64.getEncoder().encodeToString(bytes);
        return "OSS " + accessKeyId + ":" + signature;
    }
}