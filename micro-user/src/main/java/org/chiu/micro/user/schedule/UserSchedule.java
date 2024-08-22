package org.chiu.micro.user.schedule;

import lombok.RequiredArgsConstructor;
import org.chiu.micro.user.lang.StatusEnum;
import org.chiu.micro.user.req.UserEntityReq;
import org.chiu.micro.user.service.UserRoleService;
import org.chiu.micro.user.service.UserService;
import org.chiu.micro.user.vo.UserEntityVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * @Author limingjiu
 * @Date 2024/4/24 18:51
 **/
@Component
@RequiredArgsConstructor
public class UserSchedule {

    private final RedissonClient redisson;

    private final UserRoleService userRoleService;

    private final UserService userService;

    private final StringRedisTemplate redisTemplate;

    @Qualifier("commonExecutor")
    private final ExecutorService taskExecutor;

    private static final String CACHE_FINISH_FLAG = "cache_manager_finish_flag";

    private static final String MANAGER_CACHE_KEY = "managerCacheKey";

    @Scheduled(cron = "0 0 0/1 * * ?")
    public void configureTask() {

        RLock rLock = redisson.getLock(MANAGER_CACHE_KEY);
        if (Boolean.FALSE.equals(rLock.tryLock())) {
            return;
        }

        try {
            Boolean executed = redisTemplate.hasKey(CACHE_FINISH_FLAG);
            if (Boolean.FALSE.equals(executed)) {
                exec();
                redisTemplate.opsForValue().set(CACHE_FINISH_FLAG, "flag", 60, TimeUnit.SECONDS);
            }
        } finally {
            rLock.unlock();
        }
    }

    private void exec() {
        // unlock user
        CompletableFuture.runAsync(() -> {
            List<Long> ids = userService.findIdsByStatus(StatusEnum.HIDE.getCode());
            ids.forEach(id -> {
                UserEntityVo user = userRoleService.findById(id);
                user.setStatus(StatusEnum.NORMAL.getCode());
                var req = new UserEntityReq();
                req.setAvatar(user.getAvatar());
                req.setEmail(user.getEmail());
                req.setId(user.getId());
                req.setNickname(user.getNickname());
                req.setStatus(user.getStatus());
                req.setPhone(user.getPhone());
                req.setUsername(user.getUsername());
                userRoleService.saveOrUpdate(req);
            });
        }, taskExecutor);
    }

}
