package wiki.chiu.micro.auth.consumer;

import com.rabbitmq.client.Channel;
import wiki.chiu.micro.common.lang.UserAuthMenuOperateMessage;
import wiki.chiu.micro.auth.wrapper.AuthWrapper;
import wiki.chiu.micro.cache.handler.CacheEvictHandler;
import wiki.chiu.micro.cache.utils.CommonCacheKeyGenerator;
import wiki.chiu.micro.common.lang.AuthMenuOperateEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.lang.Const;

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

    private final CacheEvictHandler cacheEvictHandler;

    private final CommonCacheKeyGenerator commonCacheKeyGenerator;


    public UserRedisCacheEvictMessageListener(CacheEvictHandler cacheEvictHandler,
                                              CommonCacheKeyGenerator commonCacheKeyGenerator) {
        this.cacheEvictHandler = cacheEvictHandler;
        this.commonCacheKeyGenerator = commonCacheKeyGenerator;
    }

    @RabbitListener(queues = Const.USER_QUEUE,
            concurrency = "10",
            messageConverter = "jsonMessageConverter",
            executor = "mqExecutor")
    public void handler(UserAuthMenuOperateMessage message, Channel channel, Message msg) {
        AuthMenuOperateEnum operateEnum = AuthMenuOperateEnum.of(message.type());
        List<String> roles = message.roles();
        HashSet<String> keys = new HashSet<>();

        keys.addAll(getMenusAndButtonsKeys(roles, operateEnum));

        keys.addAll(getAuthKeys(roles, operateEnum));

        cacheEvictHandler.evictCache(keys);
    }

    private Set<String> getMenusAndButtonsKeys(List<String> roles, AuthMenuOperateEnum operateEnum) {
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
        return keys;
    }

    private Set<String> getAuthKeys(List<String> roles, AuthMenuOperateEnum operateEnum) {
        Set<String> keys = new HashSet<>();
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
        return keys;
    }
}
