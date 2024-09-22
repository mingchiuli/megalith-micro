package org.chiu.micro.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

}
