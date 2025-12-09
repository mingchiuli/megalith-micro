package wiki.chiu.micro.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;

/**
 * @author mingchiuli
 * @create 2022-04-26 10:06 PM
 */
@Configuration(proxyBeanMethods = false)
public class ThreadPoolConfig {

    @Bean("mqExecutor")
    TaskExecutor simpleAsyncTaskExecutor(ContextPropagatingTaskDecorator contextPropagatingTaskDecorator) {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setVirtualThreads(true);
        executor.setTaskTerminationTimeout(60000);
        executor.setCancelRemainingTasksOnClose(true);
        executor.setTaskDecorator(contextPropagatingTaskDecorator);
        return executor;
    }

}
