package wiki.chiu.micro.common.utils;

import wiki.chiu.micro.common.exception.MissException;

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
import java.util.Optional;

public class OssSignUtils {

    private static final String accessKeyId = Optional.ofNullable(System.getenv("megalith.blog.aliyun.access-key-id"))
            .orElse("");

    private static final String accessKeySecret = Optional.ofNullable(System.getenv("megalith.blog.aliyun.access-key-secret"))
            .orElse("");

    private static final String bucketName = Optional.ofNullable(System.getenv("megalith.blog.aliyun.oss.bucket-name"))
            .orElse("");

    private static byte[] hmacSha1(String data) {
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(OssSignUtils.accessKeySecret.getBytes(), "HmacSHA1");
            mac.init(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new MissException(e.getMessage());
        }
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    public static String getGMTDate() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.UK);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("GMT"));
        return dateTimeFormatter.format(now);
    }

    private static String buildSignData(String date, String canonicalizedResource, String methodName, String contentType) {
        //https://help.aliyun.com/zh/oss/developer-reference/include-signatures-in-the-authorization-header?spm=a2c4g.11186623.0.0.54e828efd3PoE6
        return methodName + "\n" +
                "\n" +
                contentType + "\n" +
                date + "\n" +
                canonicalizedResource;
    }

    public static String getAuthorization(String objectName, String method, String contentType) {
        String date = getGMTDate();
        String signData = buildSignData(date, "/" + bucketName + "/" + objectName, method, contentType);
        byte[] bytes = hmacSha1(signData);
        String signature = Base64.getEncoder().encodeToString(bytes);
        return "OSS " + accessKeyId + ":" + signature;
    }
}