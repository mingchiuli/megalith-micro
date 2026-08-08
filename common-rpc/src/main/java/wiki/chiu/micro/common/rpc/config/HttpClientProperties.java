package wiki.chiu.micro.common.rpc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("megalith.blog")
public record HttpClientProperties(
    @DefaultValue("http://127.0.0.1:8086/inner") String userUrl,
    @DefaultValue("http://127.0.0.1:8081/inner") String authUrl,
    @DefaultValue("http://127.0.0.1:8085/inner") String searchUrl,
    @DefaultValue("http://127.0.0.1:8082/inner") String blogUrl,
    @DefaultValue Http http,
    @DefaultValue Oss oss,
    @DefaultValue Sms sms,
    @DefaultValue Aliyun aliyun) {

  public record Http(
      @DefaultValue("10000") int userTimeout,
      @DefaultValue("300000") int ossTimeout,
      @DefaultValue("10000") int authTimeout,
      @DefaultValue("10000") int smsTimeout,
      @DefaultValue("10000") int searchTimeout,
      @DefaultValue("10000") int blogTimeout) {}

  public record Oss(String baseUrl, String endpoint) {}

  public record Sms(String baseUrl) {}

  public record Aliyun(String accessKeyId, String accessKeySecret, @DefaultValue OssSettings oss) {}

  public record OssSettings(String bucketName) {}
}
