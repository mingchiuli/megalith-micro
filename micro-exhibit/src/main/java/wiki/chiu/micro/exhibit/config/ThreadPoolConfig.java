package wiki.chiu.micro.exhibit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


/**
 * @author mingchiuli
 * @create 2022-04-26 10:06 PM
 */
@Configuration
public class ThreadPoolConfig {

    @Bean("commonExecutor")
    TaskExecutor taskExecutor(ContextPropagatingTaskDecorator contextPropagatingTaskDecorator) {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setVirtualThreads(true);
        threadPoolTaskExecutor.setTaskDecorator(contextPropagatingTaskDecorator);
        return threadPoolTaskExecutor;
    }

    @Bean("mqExecutor")
    TaskExecutor simpleAsyncTaskExecutor(ContextPropagatingTaskDecorator contextPropagatingTaskDecorator) {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setTaskDecorator(contextPropagatingTaskDecorator);
        executor.setVirtualThreads(true);
        return executor;
    }

}
