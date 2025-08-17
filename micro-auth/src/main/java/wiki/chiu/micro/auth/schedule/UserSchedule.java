package wiki.chiu.micro.auth.schedule;

import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;


/**
 * @Author limingjiu
 * @Date 2024/4/24 18:51
 **/
@Component
public class UserSchedule {

    private final RedissonClient redissonClient;

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    @Qualifier("commonExecutor")
    private final ExecutorService taskExecutor;

    public UserSchedule(RedissonClient redissonClient, UserHttpServiceWrapper userHttpServiceWrapper, @Qualifier("commonExecutor") ExecutorService taskExecutor) {
        this.redissonClient = redissonClient;
        this.userHttpServiceWrapper = userHttpServiceWrapper;
        this.taskExecutor = taskExecutor;
    }


    private void buildExecutor(Runnable task, String key, String finishKey) {

        RLock rLock = redissonClient.getLock(key);
        if (!rLock.tryLock()) {
            return;
        }

        try {
            boolean executed = redissonClient.getBucket(finishKey).isExists();
            if (!executed) {
                CompletableFuture.runAsync(task, taskExecutor);
                redissonClient.getBucket(finishKey).set("flag", Duration.ofSeconds(10));
            }
        } finally {
            rLock.unlock();
        }
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void unlockUser() {
        // unlock user
        String unlockUserExecutor = "unlockUserExecutor";
        String unlockUserExecutorFlag = "unlockUserExecutorFlag";
        buildExecutor(userHttpServiceWrapper::unlock, unlockUserExecutor, unlockUserExecutorFlag);
    }

}
