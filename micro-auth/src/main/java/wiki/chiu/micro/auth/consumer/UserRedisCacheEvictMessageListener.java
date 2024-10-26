package wiki.chiu.micro.auth.consumer;

import com.rabbitmq.client.Channel;
import wiki.chiu.micro.auth.constant.UserAuthMenuOperateMessage;
import wiki.chiu.micro.auth.wrapper.AuthWrapper;
import wiki.chiu.micro.cache.handler.CacheEvictHandler;
import wiki.chiu.micro.cache.utils.CommonCacheKeyGenerator;
import wiki.chiu.micro.common.lang.AuthMenuOperateEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author mingchiuli
 * @create 2021-12-13 11:38 AM
 */
@Component
public class UserRedisCacheEvictMessageListener {

    private static final Logger log = LoggerFactory.getLogger(UserRedisCacheEvictMessageListener.class);

    private final CommonCacheKeyGenerator commonCacheKeyGenerator;

    private final CacheEvictHandler cacheEvictHandler;

    private static final String QUEUE = "user.auth.menu.change.queue.auth";


    public UserRedisCacheEvictMessageListener(CommonCacheKeyGenerator commonCacheKeyGenerator, CacheEvictHandler cacheEvictHandler) {
        this.commonCacheKeyGenerator = commonCacheKeyGenerator;
        this.cacheEvictHandler = cacheEvictHandler;
    }

    @RabbitListener(queues = QUEUE,
            concurrency = "10",
            messageConverter = "jsonMessageConverter",
            executor = "mqExecutor")
    public void handler(UserAuthMenuOperateMessage message, Channel channel, Message msg) {
        AuthMenuOperateEnum operateEnum = AuthMenuOperateEnum.of(message.type());
        List<String> roles = message.roles();
        Set<String> keys = new HashSet<>();
        if (AuthMenuOperateEnum.MENU.equals(operateEnum) || AuthMenuOperateEnum.AUTH_AND_MENU.equals(operateEnum)) {
            Method method;
            try {
                method = AuthWrapper.class.getMethod("getCurrentUserNav", String.class);
                for (String role : roles) {
                    keys.add(commonCacheKeyGenerator.generateKey(method, role));
                }
            } catch (NoSuchMethodException e) {
                log.error("some error", e);
            }

        }

        if (AuthMenuOperateEnum.AUTH.equals(operateEnum) || AuthMenuOperateEnum.AUTH_AND_MENU.equals(operateEnum)) {
            Method getAuthoritiesByRoleCodeMethod;
            try {
                getAuthoritiesByRoleCodeMethod = AuthWrapper.class.getMethod("getAuthoritiesByRoleCode", String.class);
                for (String role : roles) {
                    keys.add(commonCacheKeyGenerator.generateKey(getAuthoritiesByRoleCodeMethod, role));
                }
            } catch (NoSuchMethodException e) {
                log.error("some error", e);
            }

            Method getAllSystemAuthoritiesMethod;
            try {
                getAllSystemAuthoritiesMethod = AuthWrapper.class.getMethod("getAllSystemAuthorities");
                keys.add(commonCacheKeyGenerator.generateKey(getAllSystemAuthoritiesMethod));
            } catch (NoSuchMethodException e) {
                log.error("some error", e);
            }
        }

        cacheEvictHandler.evictCache(keys, keys);
    }
}
