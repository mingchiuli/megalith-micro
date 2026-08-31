package wiki.chiu.micro.common.rpc.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import wiki.chiu.micro.common.exception.ValidationException;
import wiki.chiu.micro.common.rpc.OssHttpService;
import wiki.chiu.micro.common.rpc.config.HttpClientProperties;

class AliyunObjectStorageClientTest {

    private final OssHttpService http = mock(OssHttpService.class);
    private final HttpClientProperties properties =
        new HttpClientProperties(
            "http://user",
            "http://auth",
            "http://search",
            "http://blog",
            new HttpClientProperties.Http(10, 10, 10, 10, 10, 10),
            new HttpClientProperties.Oss("https://cdn.example.com/assets", "oss-cn.example.com"),
            new HttpClientProperties.Sms(null),
            new HttpClientProperties.Aliyun(
                "access-id", "access-secret", new HttpClientProperties.OssSettings("bucket")));
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-08T04:05:06Z"), ZoneOffset.UTC);
    private final AliyunObjectStorageClient client =
        new AliyunObjectStorageClient(http, properties, clock);

    @Test
    void requestHeaderAndSignatureUseExactlyTheSameDate() {
        client.put("avatar/a.png", new byte[]{1}, "image/png");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> headers = ArgumentCaptor.forClass(Map.class);
        verify(http).putOssObject(eq("avatar/a.png"), any(), headers.capture());
        String date = headers.getValue().get("Date");
        assertEquals("Sat, 8 Aug 2026 04:05:06 GMT", date);
        assertEquals(
            OssSigner.authorization(
                date, "avatar/a.png", "PUT", "image/png", "access-id", "access-secret", "bucket"),
            headers.getValue().get("Authorization"));
    }

    @Test
    void extractsOnlyObjectsFromTheConfiguredOriginAndPath() {
        assertEquals("avatar/a.png", client.objectName("https://cdn.example.com/assets/avatar/a.png"));
        assertThrows(
            ValidationException.class,
            () -> client.objectName("https://other.example.com/assets/avatar/a.png"));
        assertThrows(
            ValidationException.class,
            () -> client.objectName("https://cdn.example.com/other/avatar/a.png"));
    }
}
