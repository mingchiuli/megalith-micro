package wiki.chiu.micro.auth.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RedissonClientConfig
 */
@Configuration
public class RedissonClientConfig {

    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private String port;
    @Value("${spring.data.redis.password}")
    private String password;


    @Bean
    RedissonClient redisson() {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        config.setCodec(new StringCodec());
        singleServerConfig.setAddress("redis://" + host + ":" + port);
        singleServerConfig.setPassword(password);
        return Redisson.create(config);
    }
}