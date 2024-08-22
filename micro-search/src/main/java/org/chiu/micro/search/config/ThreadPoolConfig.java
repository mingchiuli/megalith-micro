package org.chiu.micro.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * @author mingchiuli
 * @create 2022-04-26 10:06 PM
 */
@Configuration
public class ThreadPoolConfig {

    @Bean("mqExecutor")
    TaskExecutor simpleAsyncTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setVirtualThreads(true);
        return executor;
    }

}
