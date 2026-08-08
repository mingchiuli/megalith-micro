package wiki.chiu.micro.common.rpc.storage;

import java.net.URI;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import wiki.chiu.micro.common.exception.ValidationException;
import wiki.chiu.micro.common.rpc.OssHttpService;
import wiki.chiu.micro.common.rpc.config.HttpClientProperties;

final class AliyunObjectStorageClient implements ObjectStorageClient {

  private final OssHttpService ossHttpService;
  private final HttpClientProperties properties;
  private final Clock clock;

  AliyunObjectStorageClient(
      OssHttpService ossHttpService, HttpClientProperties properties, Clock clock) {
    this.ossHttpService = ossHttpService;
    this.properties = properties;
    this.clock = clock;
  }

  @Override
  public String put(String objectName, byte[] content, String contentType) {
    ossHttpService.putOssObject(
        objectName, content, headers(objectName, HttpMethod.PUT.name(), contentType));
    return normalizedBaseUrl() + "/" + objectName;
  }

  @Override
  public void delete(String objectName) {
    ossHttpService.deleteOssObject(objectName, headers(objectName, HttpMethod.DELETE.name(), ""));
  }

  @Override
  public String objectName(String publicUrl) {
    URI base = parseUri(normalizedBaseUrl());
    URI candidate = parseUri(publicUrl).normalize();
    if (!Objects.equals(base.getScheme(), candidate.getScheme())
        || !Objects.equals(base.getHost(), candidate.getHost())
        || effectivePort(base) != effectivePort(candidate)) {
      throw new ValidationException("object URL does not belong to configured storage");
    }

    String basePath = stripTrailingSlash(base.getPath());
    String candidatePath = candidate.getPath();
    String prefix = basePath + "/";
    if (!candidatePath.startsWith(prefix) || candidatePath.length() == prefix.length()) {
      throw new ValidationException("object URL does not contain an object name");
    }
    return candidatePath.substring(prefix.length());
  }

  private Map<String, String> headers(String objectName, String method, String contentType) {
    String date = ZonedDateTime.now(clock).format(DateTimeFormatter.RFC_1123_DATE_TIME);
    HttpClientProperties.Aliyun aliyun = properties.aliyun();
    Map<String, String> headers = new HashMap<>();
    headers.put(HttpHeaders.DATE, date);
    headers.put(
        HttpHeaders.AUTHORIZATION,
        OssSigner.authorization(
            date,
            objectName,
            method,
            contentType,
            aliyun.accessKeyId(),
            aliyun.accessKeySecret(),
            aliyun.oss().bucketName()));
    headers.put(HttpHeaders.CACHE_CONTROL, "no-cache");
    headers.put(HttpHeaders.CONTENT_TYPE, contentType);
    return headers;
  }

  private String normalizedBaseUrl() {
    String baseUrl = properties.oss().baseUrl();
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new ValidationException("object storage base URL is not configured");
    }
    return stripTrailingSlash(baseUrl);
  }

  private static URI parseUri(String value) {
    try {
      URI uri = URI.create(value);
      if (!uri.isAbsolute() || uri.getHost() == null) {
        throw new IllegalArgumentException("absolute URL required");
      }
      return uri;
    } catch (IllegalArgumentException exception) {
      throw new ValidationException("invalid object URL");
    }
  }

  private static int effectivePort(URI uri) {
    if (uri.getPort() >= 0) {
      return uri.getPort();
    }
    return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
  }

  private static String stripTrailingSlash(String value) {
    return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
  }
}
