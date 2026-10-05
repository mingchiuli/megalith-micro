package wiki.chiu.micro.exhibit.adapter.out.redis;

import static wiki.chiu.micro.common.constant.Const.DAY_VISIT;
import static wiki.chiu.micro.common.constant.Const.HOT_READ;
import static wiki.chiu.micro.common.constant.Const.MONTH_VISIT;
import static wiki.chiu.micro.common.constant.Const.WEEK_VISIT;
import static wiki.chiu.micro.common.constant.Const.YEAR_VISIT;

import java.time.Duration;

import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.exhibit.application.port.out.VisitStatisticsStore;
import wiki.chiu.micro.exhibit.domain.VisitStatisticsKeys;

@Component
public class RedisVisitStatisticsStore implements VisitStatisticsStore {

    private static final Duration DAILY_CLAIM_TTL = Duration.ofHours(23);

    private final RedissonClient redissonClient;

    public RedisVisitStatisticsStore(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean claimDailyRollover() {
        return redissonClient
            .getBucket(VisitStatisticsKeys.DAILY_ROLLOVER_CLAIM)
            .setIfAbsent("1", DAILY_CLAIM_TTL);
    }

    @Override
    public void clearDailyVisits() {
        redissonClient.getBucket(DAY_VISIT).delete();
    }

    @Override
    public void clearWeeklyVisits() {
        redissonClient.getBucket(WEEK_VISIT).delete();
    }

    @Override
    public void clearMonthlyVisits() {
        redissonClient.getBucket(MONTH_VISIT).delete();
    }

    @Override
    public void clearYearlyVisits() {
        redissonClient.getBucket(YEAR_VISIT).delete();
    }

    @Override
    public void clearHotRead() {
        redissonClient.getBucket(HOT_READ).unlink();
    }
}
