package wiki.chiu.micro.user.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("megalith.user.password-lock")
public class PasswordLockProperties {

  private Duration duration = Duration.ofMinutes(15);
  private int batchSize = 100;
  private int maxBatchesPerRun = 10;

  public Duration getDuration() {
    return duration;
  }

  public void setDuration(Duration duration) {
    this.duration = duration;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public int getMaxBatchesPerRun() {
    return maxBatchesPerRun;
  }

  public void setMaxBatchesPerRun(int maxBatchesPerRun) {
    this.maxBatchesPerRun = maxBatchesPerRun;
  }
}
