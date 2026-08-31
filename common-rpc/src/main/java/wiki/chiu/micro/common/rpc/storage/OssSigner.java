package wiki.chiu.micro.common.rpc.storage;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import wiki.chiu.micro.common.exception.MissException;

final class OssSigner {

    private OssSigner() {
    }

    private static byte[] hmacSha1(String data, String accessKeySecret) {
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(accessKeySecret.getBytes(), "HmacSHA1");
            mac.init(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new MissException(e.getMessage());
        }
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String buildSignData(
        String date, String canonicalizedResource, String methodName, String contentType) {
        // https://help.aliyun.com/zh/oss/developer-reference/include-signatures-in-the-authorization-header?spm=a2c4g.11186623.0.0.54e828efd3PoE6
        return methodName + "\n" + "\n" + contentType + "\n" + date + "\n" + canonicalizedResource;
    }

    static String authorization(
        String date,
        String objectName,
        String method,
        String contentType,
        String accessKeyId,
        String accessKeySecret,
        String bucketName) {
        String signData = buildSignData(date, "/" + bucketName + "/" + objectName, method, contentType);
        byte[] bytes = hmacSha1(signData, accessKeySecret);
        String signature = Base64.getEncoder().encodeToString(bytes);
        return "OSS " + accessKeyId + ":" + signature;
    }
}
