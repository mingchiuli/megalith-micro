package wiki.chiu.micro.common.scheduling.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("megalith.task-lock")
public class TaskLockProperties {

    private String environment = "default";

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}
