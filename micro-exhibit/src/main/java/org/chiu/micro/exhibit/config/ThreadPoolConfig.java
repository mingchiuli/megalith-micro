package org.chiu.micro.exhibit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.util.concurrent.*;

/**
 * @author mingchiuli
 * @create 2022-04-26 10:06 PM
 */
@Configuration
public class ThreadPoolConfig {

    @Bean("commonExecutor")
    ExecutorService executorService() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean("mqExecutor")
    TaskExecutor simpleAsyncTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setVirtualThreads(true);
        return executor;
    }

}
