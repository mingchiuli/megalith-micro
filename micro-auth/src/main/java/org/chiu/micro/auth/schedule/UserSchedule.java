package org.chiu.micro.auth.schedule;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.chiu.micro.auth.rpc.wrapper.UserHttpServiceWrapper;


/**
 * @Author limingjiu
 * @Date 2024/4/24 18:51
 **/
@Component
@RequiredArgsConstructor
public class UserSchedule {

    private final RedissonClient redissonClient;
    
    private final UserHttpServiceWrapper userHttpServiceWrapper;

    @Qualifier("commonExecutor")
    private final ExecutorService taskExecutor;

    private static final String CACHE_FINISH_FLAG = "cache_manager_finish_flag";

    private static final String MANAGER_CACHE_KEY = "managerCacheKey";

    @Scheduled(cron = "0 0 0/1 * * ?")
    public void configureTask() {

        RLock rLock = redissonClient.getLock(MANAGER_CACHE_KEY);
        if (Boolean.FALSE.equals(rLock.tryLock())) {
            return;
        }

        try {
            Boolean executed = redissonClient.getBucket(CACHE_FINISH_FLAG).isExists();
            if (Boolean.FALSE.equals(executed)) {
                exec();
                redissonClient.getBucket(CACHE_FINISH_FLAG).set("flag", Duration.ofSeconds(60));
            }
        } finally {
            rLock.unlock();
        }
    }

    private void exec() {
        // unlock user
        CompletableFuture.runAsync(() -> {
            userHttpServiceWrapper.unlock();    
        }, taskExecutor);
    }

}
